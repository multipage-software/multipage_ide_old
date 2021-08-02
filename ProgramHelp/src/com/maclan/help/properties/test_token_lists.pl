/**
 * Test token list variants.
 */
test :-
    test_tags,
    test_good_tag,
    test_good_tag_ws,
    test_bad_tag,
    test_good_full_tag,
    test_property_area,
    test_property_r,
    test_property_sep,
    test_property_sep_property,
    test_property_sep_property_sep_property.
    
my_test :-
    print('test_tags: '), get_suggestions([tag_start('AREA_ID'),whitespace_separator], Suggestions), print(Suggestions), write('\n\n').
    
test_tags :-
    print('test_tags: '), get_suggestions([tag_start('AREA')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').

test_good_tag :-
    print('test_good_tag: '), get_suggestions([tag_start('AREA_NAME')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
    
test_good_tag_ws :-
    print('test_good_tag_ws: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
    
test_bad_tag :-
    print('test_bad_tag: no suggestions'), \+get_suggestions([tag_start('AREA_DESCRIPTION')], _), write('\n\n').
    
test_good_full_tag :-
    print('test_good_full_tag: no suggestions'), \+get_suggestions([tag_start('AREA_NAME'), whitespace_separator, tag_closing], _), write('\n\n').

test_property_area :-
    print('test_property_area: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator, property_name('area')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
    
test_property_r :-
    print('test_property_r: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator, property_name('r')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
    
test_property_sep :-
    print('test_property_sep: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator, property_name('areaId'), property_separator], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').

test_property_sep_property :-
    print('test_property_sep_property: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator, property_name('areaId'), property_separator, property_name('area')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
    
test_property_sep_property_sep_property :-
    print('test_property_sep_property_sep_property: '), get_suggestions([tag_start('AREA_NAME'), whitespace_separator, property_name('areaId'), property_separator, property_name('areaName'), property_separator, property_name('r')], SUGGESTIONS), print(SUGGESTIONS), write('\n\n').
