 /**
  * Copyright 2010-2021 (C) vakol
  * 
  * Created on : 01-07-2021
  *
  * Maclan macrolanguage intellisense suggestions.
  *
  */

/* Provides intellisense suggestions for a token list. */
get_suggestions(TOKENS, SUGGESTIONS) :-
    get_tag(TOKENS, TAG, TAG_START, TAG_END),
    get_last(TOKENS, LAST_TOKEN),
    (
        /* Tag start. */
        tag_start(TAG, TAG_START, TAG_END) = LAST_TOKEN,
        setof(SUGGESTION, statement_match(maclan(tag(TAG, match(TAG_START, TAG_END))), SUGGESTION), SUGGESTIONS), !
        ;
        /* Tag end. */
        get_end_tag(TOKENS, END_TAG),
        end_tag(END_TAG, END_TAG_START, END_TAG_END) = LAST_TOKEN,
        setof(SUGGESTION, statement_match(maclan(tag(TAG, match(END_TAG_START, END_TAG_END))), SUGGESTION), SUGGESTIONS), !
        ;
        /* Tag propeties and values. */
        \+has_closing(TOKENS, _, _),
        (
            /* Suggest all available properties. */
            whitespace_separator(_WS_START, WS_END) = LAST_TOKEN,
            setof(SUGGESTION, statement_match(maclan(tag(TAG, match(TAG_START, TAG_END)), property(match(WS_END, WS_END))), SUGGESTION), SUGGESTIONS), !
            ;
            /* Suggest properties or values. */
            get_properties(TOKENS, PROPERTIES),
            get_last(PROPERTIES, LAST_PROPERTY),
            LAST_PROPERTY =.. [PROPERTY_NAME, PROPERTY_START, PROPERTY_END],
            exclude_item(PROPERTIES, LAST_PROPERTY, EXCLUDED_PROPERTIES),
            (
                property_separator(_PS_START, PS_END) = LAST_TOKEN,
                ALL_EXCLUDED_PROPERTIES = [LAST_PROPERTY|EXCLUDED_PROPERTIES],
                setof(SUGGESTION, statement_match(maclan(tag(TAG, match(TAG_START, TAG_END)), property(match(PS_END, PS_END))), ALL_EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
                ;
                property_name(PROPERTY_NAME, PROPERTY_START, PROPERTY_END) = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG, match(TAG_START, TAG_END)), property(PROPERTY_NAME, match(PROPERTY_START, PROPERTY_END))), EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
                ;
                equal_sign(_ES_START, ES_END) = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG, match(TAG_START, TAG_END)), property(PROPERTY_NAME, match(ES_END, ES_END)), value('')), EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
            )
        )
    ).

/* Gets tag name. */
get_tag([tag_start(TAG, TAG_START, TAG_END)|_], TAG, TAG_START, TAG_END).

/* Returns end of the complex tag. */
get_end_tag([H|T], END_TAG) :-
    H = end_tag(END_TAG), !;
    get_end_tag(T, END_TAG).

/* Extracts properties from the list. */
get_properties(TOKENS, PROPERTIES) :-
    findall(PROPERTY, get_property(TOKENS, PROPERTY), PROPERTIES).
    
get_property([H|T], PROPERTY) :-
    H = property_name(NAME, PROPERTY_START, PROPERTY_END),
    PROPERTY =..[NAME, PROPERTY_START, PROPERTY_END];
    get_property(T, PROPERTY).

/* Extracts values from the list. */
get_values(TOKENS, VALUES) :-
    findall(VALUE, get_value(TOKENS, VALUE), VALUES).

get_value([H|T], VALUE) :-
    H = property_value(VALUE);
    get_value(T, VALUE).

/* Succeeded if the list contains closing bracket of the tag. */
has_closing([H|T], TAG_CLOSING_START, TAG_CLOSING_END) :-
    H = tag_closing(']', TAG_CLOSING_START, TAG_CLOSING_END), !;
    has_closing(T, TAG_CLOSING_START, TAG_CLOSING_END).

/* Gets last item from the list. */
get_last([H|T], ITEM) :-
    T = [], H = ITEM, !;
    get_last(T, ITEM).
    
/* Exclude item instances from the list. */
exclude_item([], _, []).

exclude_item([H|T], ITEM, [H|NEW_T]) :-
    ITEM \= H,
    exclude_item(T, ITEM, NEW_T).
    
exclude_item([ITEM|T], ITEM, NEW_T) :-
    exclude_item(T, ITEM, NEW_T).

/* Tag without property. */
statement_match(maclan(tag(TAG_HEAD, match(TAG_START, TAG_END))), maclan(tag(TAG, type(TAG_TYPE), distance(TAG_DISTANCE), match(TAG_START, TAG_END)))) :-
    maclan(tag(TAG), type(TAG_TYPE)),
    head_match(TAG_HEAD, TAG, TAG_DISTANCE).
    
/* Tag and properties without value. */
statement_match(maclan(tag(TAG_HEAD, match(TAG_START, TAG_END)), property(match(PROPERTY_START, PROPERTY_END))), maclan(tag(TAG, type(TAG_TYPE), distance(TAG_DISTANCE), match(TAG_START, TAG_END)), property(PROPERTY, type(PROPERTY_TYPE), distance(0), match(PROPERTY_START, PROPERTY_END)))) :-
    maclan(tag(TAG), property(PROPERTY), type(PROPERTY_TYPE)),
    maclan(tag(TAG), type(TAG_TYPE)),
    head_match(TAG_HEAD, TAG, TAG_DISTANCE).
    
/* Tag and properties without value. */
statement_match(maclan(tag(TAG_HEAD, match(TAG_START, TAG_END)), property(match(PROPERTY_START, PROPERTY_END))), EXCLUDED_PROPERTIES, maclan(tag(TAG, type(TAG_TYPE), distance(TAG_DISTANCE), match(TAG_START, TAG_END)), property(PROPERTY, type(PROPERTY_TYPE), distance(0), match(PROPERTY_START, PROPERTY_END)))) :-
    maclan(tag(TAG), property(PROPERTY), type(PROPERTY_TYPE)),
    maclan(tag(TAG), type(TAG_TYPE)),
    head_match(TAG_HEAD, TAG, TAG_DISTANCE),
    \+member(PROPERTY, EXCLUDED_PROPERTIES).

/* Tag and property without value. */
statement_match(maclan(tag(TAG_HEAD, match(TAG_START, TAG_END)), property(PROPERTY_HEAD, match(PROPERTY_START, PROPERTY_END))), EXCLUDED_PROPERTIES, maclan(tag(TAG, type(TAG_TYPE), distance(TAG_DISTANCE), match(TAG_START, TAG_END)), property(PROPERTY, type(PROPERTY_TYPE), distance(PROPERTY_DISTANCE), match(PROPERTY_START, PROPERTY_END)))) :-
    maclan(tag(TAG), property(PROPERTY), type(PROPERTY_TYPE)),
    maclan(tag(TAG), type(TAG_TYPE)),
    head_match(TAG_HEAD, TAG, TAG_DISTANCE),
    head_match(PROPERTY_HEAD, PROPERTY, PROPERTY_DISTANCE),
    \+member(PROPERTY, EXCLUDED_PROPERTIES).

/* Match text with a pattern text for IntelliSense purposes. */
head_match(TEXT, TEXT_PATTERN, DISTANCE) :-

    /* Check text head. */
    atom_concat(TEXT, '*', WILDCARDED_TEXT),
    wildcard_match(WILDCARDED_TEXT, TEXT_PATTERN),
    DISTANCE is 0, !;
    
    /* Otherwise use Levenshtein distance to decide. */
    text_padding(TEXT, TEXT_PATTERN),
    levenshtein_distance(TEXT, TEXT_PATTERN, DISTANCE).
