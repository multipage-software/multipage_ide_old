 /**
 * Maclan macrolanguage intellisense suggestions.
 */ 
 
suggestion(TERM, TERM_SUGGESTION) :-
    statement_match(TERM, TERM_SUGGESTION).

begin_match(TEXT_BEGINS, TEXT) :-
    atom_concat(TEXT_BEGINS, '*', PATTERN),
    wildcard_match(PATTERN, TEXT).
    
statement_match(maclan(tag(TAG_BEGIN)), maclan(tag(TAG))) :-
    maclan(tag(TAG)),
    begin_match(TAG_BEGIN, TAG).
    
/**
 * Maclan tag definitions.
 */
maclan(tag('TAG')).
maclan(tag('TAGS')).
maclan(tag('LOOP')).
maclan(tag('TAG'), property_name('slot')).
maclan(tag('TAGS'), property_name('slot')).
maclan(tag('LOOP'), property_name('count')).
maclan(tag('TAG'), property_name('inherits'), property_value('true')).
maclan(tag('TAG'), property_name('inherits'), property_value('false')).