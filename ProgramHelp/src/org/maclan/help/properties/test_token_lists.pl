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
    test_property_sep_property_sep_property,
    print('ALL DONE').

my_test :-
    print('my_test: '), get_suggestions([tag_start('TAGabcdef', 2, 11),whitespace_separator(11, 12),property_name(h, 12, 13)],Suggestions), print(Suggestions), write('\n\n').
        
test_tags :-
    print('test_tags: '), get_suggestions([tag_start('AREAabcde', 1, 10)], Suggestions), print(Suggestions), write('\n\n').

test_good_tag :-
    print('test_good_tag: '), get_suggestions([tag_start('AREA_NAME', 1, 10)], Suggestions), print(Suggestions), write('\n\n').
    
test_good_tag_ws :-
    print('test_good_tag_ws: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5)], Suggestions), print(Suggestions), write('\n\n').
    
test_bad_tag :-
    print('test_bad_tag: no suggestions'), \+get_suggestions([tag_start('AREA_DESCRIPTION', 1, 3)], _), write('\n\n').
    
test_good_full_tag :-
    print('test_good_full_tag: no suggestions'), \+get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), tag_closing(5, 6)], _), write('\n\n').

test_property_area :-
    print('test_property_area: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), property_name('area', 5 ,8)], Suggestions), print(Suggestions), write('\n\n').
    
test_property_r :-
    print('test_property_r: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), property_name('r', 5, 8)], Suggestions), print(Suggestions), write('\n\n').
    
test_property_sep :-
    print('test_property_sep: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), property_name('areaId', 5, 8), property_separator(8, 10)], Suggestions), print(Suggestions), write('\n\n').

test_property_sep_property :-
    print('test_property_sep_property: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), property_name('areaId', 5, 8), property_separator(8, 10), property_name('area', 10, 13)], Suggestions), print(Suggestions), write('\n\n').
    
test_property_sep_property_sep_property :-
    print('test_property_sep_property_sep_property: '), get_suggestions([tag_start('AREA_NAME', 1, 3), whitespace_separator(4, 5), property_name('areaId', 5, 8), property_separator(8, 10), property_name('areaName', 10, 13), property_separator(13, 14), property_name('r', 14, 15)], Suggestions), print(Suggestions), write('\n\n').
