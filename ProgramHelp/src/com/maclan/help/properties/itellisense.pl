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
    get_tag(TOKENS, TAG),
    get_last(TOKENS, LAST_TOKEN),
    (
        /* Tag start. */
        tag_start(TAG) = LAST_TOKEN,
        setof(SUGGESTION, statement_match(maclan(tag(TAG)), SUGGESTION), SUGGESTIONS), !
        ;
        /* Tag end. */
        get_end_tag(TOKENS, END_TAG),
        end_tag(END_TAG) = LAST_TOKEN,
        setof(SUGGESTION, statement_match(maclan(tag(TAG)), SUGGESTION), SUGGESTIONS), !
        ;
        /* Tag propeties and values. */
        \+has_closing(TOKENS),
        (
            /* Suggest all available properties. */
            white_space_speparator = LAST_TOKEN,
            setof(SUGGESTION, statement_match(maclan(tag(TAG), property), SUGGESTION), SUGGESTIONS), !
            ;
            /* Suggest properties or values. */
            get_properties(TOKENS, PROPERTIES),
            get_last(PROPERTIES, LAST_PROPERTY),
            exclude_item(PROPERTIES, LAST_PROPERTY, EXCLUDED_PROPERTIES),
            (
                property_name(LAST_PROPERTY) = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG), property(LAST_PROPERTY)), EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
                ;
                equal_sign = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG), property(LAST_PROPERTY), value('')), EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
                ;
                get_values(TOKENS, VALUES),
                get_last(VALUES, LAST_VALUE),
                property_value(LAST_VALUE) = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG), property(LAST_PROPERTY), value(LAST_VALUE)), EXCLUDED_PROPERTIES, SUGGESTION), SUGGESTIONS), !
                ;
                property_separator = LAST_TOKEN,
                setof(SUGGESTION, statement_match(maclan(tag(TAG), property), SUGGESTION), SUGGESTIONS), !
            )
        )
    ).

/* Gets tag name. */
get_tag([tag_start(TAG)|_], TAG).

/* Returns end of the complex tag. */
get_end_tag([H|T], END_TAG) :-
    H = end_tag(END_TAG), !;
    get_end_tag(T, END_TAG).

/* Extracts properties from the list. */
get_properties(TOKENS, NAMES) :-
    findall(NAME, get_property(TOKENS, NAME), NAMES).
    
get_property([H|T], NAME) :-
    H = property_name(NAME);
    get_property(T, NAME).

/* Extracts values from the list. */
get_values(TOKENS, VALUES) :-
    findall(VALUE, get_value(TOKENS, VALUE), VALUES).

get_value([H|T], VALUE) :-
    H = property_value(VALUE);
    get_value(T, VALUE).

/* Succeeded if the list contains closing bracket of the tag. */
has_closing([H|T]) :-
    H = tag_closing(']'), !;
    has_closing(T).

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
statement_match(maclan(tag(TAG_BEGIN)), maclan(tag(TAG))) :-
    maclan(tag(TAG)),
    begin_match(TAG_BEGIN, TAG).
    
/* Tag and properties without value. */
statement_match(maclan(tag(TAG_BEGIN), property), maclan(tag(TAG), property(PROPERTY))) :-
    maclan(tag(TAG), property(PROPERTY)),
    begin_match(TAG_BEGIN, TAG).

/* Tag and property without value. */
statement_match(maclan(tag(TAG_BEGIN), property(PROPERTY_BEGIN)), EXCLUDED_PROPERTIES, maclan(tag(TAG), property(PROPERTY))) :-
    maclan(tag(TAG), property(PROPERTY)),
    begin_match(TAG_BEGIN, TAG),
    begin_match(PROPERTY_BEGIN, PROPERTY),
    \+member(PROPERTY, EXCLUDED_PROPERTIES).
    
/* Tag and property with value. */
statement_match(maclan(tag(TAG_BEGIN), property(PROPERTY_BEGIN), value(VALUE_BEGIN)), EXCLUDED_PROPERTIES, maclan(tag(TAG), property(PROPERTY), value(VALUE))) :-
    maclan(tag(TAG), property(PROPERTY), value(VALUE)),
    begin_match(TAG_BEGIN, TAG),
    begin_match(PROPERTY_BEGIN, PROPERTY),
    begin_match(VALUE_BEGIN, VALUE),
    \+member(PROPERTY, EXCLUDED_PROPERTIES).

/* Match leading part of the text. */
begin_match(TEXT_BEGINS, TEXT) :-
    atom_concat(TEXT_BEGINS, '*', PATTERN),
    wildcard_match(PATTERN, TEXT).