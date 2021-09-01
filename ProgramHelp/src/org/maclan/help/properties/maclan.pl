maclan(tag('AREA_NAME'), type('Simple')).
maclan(tag('AREA_ID'), type('Simple')).
maclan(tag('AREA_ALIAS'), type('Simple')).
maclan(tag('SUBAREAS'), type('Simple')).
maclan(tag('SUPERAREAS'), type('Simple')).
maclan(tag('TAG'), type('Simple')).
maclan(tag('LANG_FLAG'), type('Simple')).
maclan(tag('LANG_DESCRIPTION'), type('Simple')).
maclan(tag('LANG_ALIAS'), type('Simple')).
maclan(tag('LANG_ID'), type('Simple')).
maclan(tag('LANGUAGES'), type('Complex')).
maclan(tag('RESOURCE_ID'), type('Simple')).
maclan(tag('RESOURCE_EXT'), type('Simple')).
maclan(tag('RESOURCE_VALUE'), type('Simple')).
maclan(tag('VERSION_ANCHOR'), type('Simple')).
maclan(tag('VERSION_URL'), type('Simple')).
maclan(tag('VERSION_NAME'), type('Simple')).
maclan(tag('VERSION_ID'), type('Simple')).
maclan(tag('LIST'), type('Complex')).
maclan(tag('LAST'), type('Simple')).
maclan(tag('LOOP'), type('Complex')).
maclan(tag('IMAGE'), type('Simple')).
maclan(tag('VAR'), type('Simple')).
maclan(tag('SET'), type('Simple')).
maclan(tag('GET'), type('Simple')).
maclan(tag('A'), type('Complex')).
maclan(tag('ANCHOR'), type('Complex')).
maclan(tag('REM'), type('Complex')).
maclan(tag('IF'), type('ComplexCondition')).
maclan(tag('ELSEIF'), type('ComplexCondition')).
maclan(tag('ELSE'), type('ComplexCondition')).
maclan(tag('PROCEDURE'), type('Complex')).
maclan(tag('CALL'), type('SimpleOrComplex')).
maclan(tag('PACK'), type('Complex')).
maclan(tag('BLOCK'), type('Complex')).
maclan(tag('OUTPUT'), type('Complex')).
maclan(tag('TRACE'), type('Simple')).
maclan(tag('BREAK'), type('Simple')).
maclan(tag('PPTEXT'), type('Complex')).
maclan(tag('URL'), type('Simple')).
maclan(tag('USING'), type('Simple')).
maclan(tag('RENDER_CLASS'), type('Simple')).
maclan(tag('TIMOEOUT'), type('Simple')).
maclan(tag('BOOKMARK'), type('Simple')).
maclan(tag('REPLACE_BOOKMARK'), type('Complex')).
maclan(tag('INCLUDE_ONCE'), type('Complex')).
maclan(tag('LITERAL'), type('Complex')).
maclan(tag('PRAGMA'), type('Simple')).
maclan(tag('JAVASCRIPT'), type('Complex')).
maclan(tag('INDENT'), type('Complex')).
maclan(tag('NOINDENT'), type('Complex')).
maclan(tag('CSS_LOOKUP_TABLE'), type('Complex')).
maclan(tag('UNZIP'), type('Simple')).
maclan(tag('RUN'), type('Simple')).
maclan(tag('REDIRECT'), type('Simple')).
maclan(tag('TRAY_MENU'), type('Simple')).

maclan(tag('AREA_NAME'), property('areaId'), type('AreaId')).
maclan(tag('AREA_NAME'), property('areaAlias'), type('AreaAlias')).
maclan(tag('AREA_NAME'), property('area'), type('Area')).
maclan(tag('AREA_NAME'), property('startArea'), type('Area')).
maclan(tag('AREA_NAME'), property('homeArea'), type('Area')).
maclan(tag('AREA_NAME'), property('requestedArea'), type('Area')).
maclan(tag('AREA_NAME'), property('thisArea'), type('Area')).
maclan(tag('AREA_NAME'), property('areaSlot'), type('SlotName')).

maclan(tag('AREA_ID'), property('areaId'), type('AreaId')).
maclan(tag('AREA_ID'), property('areaAlias'), type('AreaAlias')).
maclan(tag('AREA_ID'), property('area'), type('Area')).
maclan(tag('AREA_ID'), property('startArea'), type('Area')).
maclan(tag('AREA_ID'), property('homeArea'), type('Area')).
maclan(tag('AREA_ID'), property('requestedArea'), type('Area')).
maclan(tag('AREA_ID'), property('thisArea'), type('Area')).
maclan(tag('AREA_ID'), property('areaSlot'), type('SlotName')).

maclan(tag('AREA_ALIAS'), property('areaId'), type('AreaId')).
maclan(tag('AREA_ALIAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('AREA_ALIAS'), property('area'), type('Area')).
maclan(tag('AREA_ALIAS'), property('startArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('homeArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('requestedArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('thisArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('areaSlot'), type('SlotName')).

maclan(tag('SUBAREAS'), property('list'), type('List')).
maclan(tag('SUBAREAS'), property('first'), type('Any')).
maclan(tag('SUBAREAS'), property('last'), type('Any')).
maclan(tag('SUBAREAS'), property('cond'), type('Boolean')).
maclan(tag('SUBAREAS'), property('reversed'), type('Void')).
maclan(tag('SUBAREAS'), property('areaId'), type('AreaId')).
maclan(tag('SUBAREAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('SUBAREAS'), property('area'), type('Area')).
maclan(tag('SUBAREAS'), property('startArea'), type('Area')).
maclan(tag('SUBAREAS'), property('homeArea'), type('Area')).
maclan(tag('SUBAREAS'), property('requestedArea'), type('Area')).
maclan(tag('SUBAREAS'), property('thisArea'), type('Area')).
maclan(tag('SUBAREAS'), property('areaSlot'), type('SlotName')).

maclan(tag('SUPERAREAS'), property('list'), type('List')).
maclan(tag('SUPERAREAS'), property('first'), type('Any')).
maclan(tag('SUPERAREAS'), property('last'), type('Any')).
maclan(tag('SUPERAREAS'), property('cond'), type('Boolean')).
maclan(tag('SUPERAREAS'), property('reversed'), type('Void')).
maclan(tag('SUPERAREAS'), property('areaId'), type('AreaId')).
maclan(tag('SUPERAREAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('SUPERAREAS'), property('area'), type('Area')).
maclan(tag('SUPERAREAS'), property('startArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('homeArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('requestedArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('thisArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('areaSlot'), type('SlotName')).

maclan(tag('TAG'), property('slot'), type('String')).
maclan(tag('TAG'), property('local'), type('Void')).
maclan(tag('TAG'), property('skipDefault'), type('Void')).
maclan(tag('TAG'), property('parent'), type('Void')).
maclan(tag('TAG'), property('enableSpecialValue'), type('Void')).
maclan(tag('TAG'), property('areaId'), type('AreaId')).
maclan(tag('TAG'), property('areaAlias'), type('AreaAlias')).
maclan(tag('TAG'), property('area'), type('Area')).
maclan(tag('TAG'), property('startArea'), type('Area')).
maclan(tag('TAG'), property('homeArea'), type('Area')).
maclan(tag('TAG'), property('requestedArea'), type('Area')).
maclan(tag('TAG'), property('thisArea'), type('Area')).
maclan(tag('TAG'), property('areaSlot'), type('SlotName')).

maclan(tag('LANGUAGES'), property('divider'), type('String')).
maclan(tag('LANGUAGES'), property('transparent'), type('Void')).

maclan(tag('RESOURCE_ID'), property('res'), type('String')).
maclan(tag('RESOURCE_ID'), property('render'), type('Void')).
maclan(tag('RESOURCE_ID'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_ID'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_ID'), property('area'), type('Area')).
maclan(tag('RESOURCE_ID'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('areaSlot'), type('SlotName')).

maclan(tag('RESOURCE_EXT'), property('res'), type('String')).
maclan(tag('RESOURCE_EXT'), property('render'), type('Void')).
maclan(tag('RESOURCE_EXT'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_EXT'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_EXT'), property('area'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('areaSlot'), type('SlotName')).

maclan(tag('RESOURCE_VALUE'), property('res'), type('String')).
maclan(tag('RESOURCE_VALUE'), property('render'), type('Void')).
maclan(tag('RESOURCE_VALUE'), property('coding'), type('String')).
maclan(tag('RESOURCE_VALUE'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_VALUE'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_VALUE'), property('area'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('areaSlot'), type('SlotName')).

maclan(tag('VERSION_URL'), property('areaId'), type('AreaId')).
maclan(tag('VERSION_URL'), property('areaAlias'), type('AreaAlias')).
maclan(tag('VERSION_URL'), property('area'), type('Area')).
maclan(tag('VERSION_URL'), property('startArea'), type('Area')).
maclan(tag('VERSION_URL'), property('homeArea'), type('Area')).
maclan(tag('VERSION_URL'), property('requestedArea'), type('Area')).
maclan(tag('VERSION_URL'), property('thisArea'), type('Area')).
maclan(tag('VERSION_URL'), property('areaSlot'), type('SlotName')).

maclan(tag('LIST'), property('list'), type('List')).
maclan(tag('LIST'), property('iterator'), type('Variable')).
maclan(tag('LIST'), property('item'), type('Variable')).
maclan(tag('LIST'), property('divider'), type('String')).
maclan(tag('LIST'), property('local'), type('Void')).
maclan(tag('LIST'), property('break'), type('Void')).
maclan(tag('LIST'), property('discard'), type('Void')).
maclan(tag('LIST'), property('transparent'), type('Void')).

maclan(tag('LAST'), property('discard'), type('Void')).

maclan(tag('LOOP'), property('count'), type('Long')).
maclan(tag('LOOP'), property('divider'), type('String')).
maclan(tag('LOOP'), property('index'), type('Long')).
maclan(tag('LOOP'), property('break'), type('Void')).
maclan(tag('LOOP'), property('discard'), type('Void')).
maclan(tag('LOOP'), property('from'), type('Long')).
maclan(tag('LOOP'), property('to'), type('Long')).
maclan(tag('LOOP'), property('step'), type('Long')).
maclan(tag('LOOP'), property('transparent'), type('Void')).

maclan(tag('IMAGE'), property('res'), type('String')).
maclan(tag('IMAGE'), property('areaId'), type('AreaId')).
maclan(tag('IMAGE'), property('areaAlias'), type('AreaAlias')).
maclan(tag('IMAGE'), property('area'), type('Area')).
maclan(tag('IMAGE'), property('startArea'), type('Area')).
maclan(tag('IMAGE'), property('homeArea'), type('Area')).
maclan(tag('IMAGE'), property('requestedArea'), type('Area')).
maclan(tag('IMAGE'), property('thisArea'), type('Area')).
maclan(tag('IMAGE'), property('areaSlot'), type('SlotName')).

maclan(tag('GET'), property('exp'), type('Expression')).

maclan(tag('A'), property('href'), type('String')).
maclan(tag('A'), property('areaId'), type('AreaId')).
maclan(tag('A'), property('areaAlias'), type('AreaAlias')).
maclan(tag('A'), property('area'), type('Area')).
maclan(tag('A'), property('startArea'), type('Area')).
maclan(tag('A'), property('homeArea'), type('Area')).
maclan(tag('A'), property('requestedArea'), type('Area')).
maclan(tag('A'), property('thisArea'), type('Area')).
maclan(tag('A'), property('areaSlot'), type('SlotName')).

maclan(tag('ANCHOR'), property('href'), type('String')).
maclan(tag('ANCHOR'), property('areaId'), type('AreaId')).
maclan(tag('ANCHOR'), property('areaAlias'), type('AreaAlias')).
maclan(tag('ANCHOR'), property('area'), type('Area')).
maclan(tag('ANCHOR'), property('startArea'), type('Area')).
maclan(tag('ANCHOR'), property('homeArea'), type('Area')).
maclan(tag('ANCHOR'), property('requestedArea'), type('Area')).
maclan(tag('ANCHOR'), property('thisArea'), type('Area')).
maclan(tag('ANCHOR'), property('areaSlot'), type('SlotName')).

maclan(tag('IF'), property('cond'), type('Boolean')).
maclan(tag('ELSEIF'), property('cond'), type('Boolean')).

maclan(tag('PROCEDURE'), property('name'), type('ProcedureName')).
maclan(tag('PROCEDURE'), property('$name'), type('ProcedureName')).
maclan(tag('PROCEDURE'), property('$useLast'), type('Void')).
maclan(tag('PROCEDURE'), property('$global'), type('Void')).
maclan(tag('PROCEDURE'), property('$returnText'), type('Void')).
maclan(tag('PROCEDURE'), property('$inner'), type('Void')).
maclan(tag('PROCEDURE'), property('$transparent'), type('Void')).

maclan(tag('CALL'), property('name'), type('Procedure')).
maclan(tag('CALL'), property('$name'), type('Procedure')).
maclan(tag('CALL'), property('$areaId'), type('Long')).
maclan(tag('CALL'), property('$areaAlias'), type('String')).
maclan(tag('CALL'), property('$area'), type('Area')).
maclan(tag('CALL'), property('$startArea'), type('Area')).
maclan(tag('CALL'), property('$homeArea'), type('Area')).
maclan(tag('CALL'), property('$requestedArea'), type('Area')).
maclan(tag('CALL'), property('$thisArea'), type('Area')).
maclan(tag('CALL'), property('$areaSlot'), type('SlotName')).
maclan(tag('CALL'), property('$parent'), type('Void')).
maclan(tag('CALL'), property('$inner'), type('Void')).

maclan(tag('PACK'), property('strong'), type('Void')).
maclan(tag('PACK'), property('trim'), type('Void')).
maclan(tag('PACK'), property('trimBegin'), type('Void')).
maclan(tag('PACK'), property('trimEnd'), type('Void')).

maclan(tag('BLOCK'), property('areaId'), type('AreaId')).
maclan(tag('BLOCK'), property('areaAlias'), type('AreaAlias')).
maclan(tag('BLOCK'), property('area'), type('Area')).
maclan(tag('BLOCK'), property('startArea'), type('Area')).
maclan(tag('BLOCK'), property('homeArea'), type('Area')).
maclan(tag('BLOCK'), property('requestedArea'), type('Area')).
maclan(tag('BLOCK'), property('thisArea'), type('Area')).
maclan(tag('BLOCK'), property('areaSlot'), type('SlotName')).
maclan(tag('BLOCK'), property('transparent'), type('Void')).

maclan(tag('TRACE'), property('name'), type('String')).
maclan(tag('TRACE'), property('simple'), type('Void')).

maclan(tag('BREAK'), property('name'), type('String')).
maclan(tag('BREAK'), property('no'), type('Void')).

maclan(tag('URL'), property('areaId'), type('AreaId')).
maclan(tag('URL'), property('areaAlias'), type('AreaAlias')).
maclan(tag('URL'), property('area'), type('Area')).
maclan(tag('URL'), property('startArea'), type('Area')).
maclan(tag('URL'), property('homeArea'), type('Area')).
maclan(tag('URL'), property('requestedArea'), type('Area')).
maclan(tag('URL'), property('thisArea'), type('Area')).
maclan(tag('URL'), property('areaSlot'), type('SlotName')).
maclan(tag('URL'), property('res'), type('String')).
maclan(tag('URL'), property('localhost'), type('Void')).
maclan(tag('URL'), property('langAlias'), type('String')).
maclan(tag('URL'), property('versionAlias'), type('String')).
maclan(tag('URL'), property('download'), type('Void')).
maclan(tag('URL'), property('file'), type('String')).

maclan(tag('USING'), property('res'), type('String')).
maclan(tag('USING'), property('resId'), type('Long')).
maclan(tag('USING'), property('file'), type('String')).
maclan(tag('USING'), property('extract'), type('Boolean')).
maclan(tag('USING'), property('encoding'), type('String')).
maclan(tag('USING'), property('areaId'), type('AreaId')).
maclan(tag('USING'), property('areaAlias'), type('AreaAlias')).
maclan(tag('USING'), property('area'), type('Area')).
maclan(tag('USING'), property('startArea'), type('Area')).
maclan(tag('USING'), property('homeArea'), type('Area')).
maclan(tag('USING'), property('requestedArea'), type('Area')).
maclan(tag('USING'), property('thisArea'), type('Area')).
maclan(tag('USING'), property('areaSlot'), type('SlotName')).

maclan(tag('PRAGMA'), property('php'), type('Boolean')).
maclan(tag('PRAGMA'), property('tabulator'), type('String')).
maclan(tag('PRAGMA'), property('webInterface'), type('FolderPath')).
maclan(tag('PRAGMA'), property('metaCharset'), type('String')).
maclan(tag('PRAGMA'), property('metaInfo'), type('String')).

maclan(tag('UNZIP'), property('areaId'), type('AreaId')).
maclan(tag('UNZIP'), property('areaAlias'), type('AreaAlias')).
maclan(tag('UNZIP'), property('area'), type('Area')).
maclan(tag('UNZIP'), property('startArea'), type('Area')).
maclan(tag('UNZIP'), property('homeArea'), type('Area')).
maclan(tag('UNZIP'), property('requestedArea'), type('Area')).
maclan(tag('UNZIP'), property('thisArea'), type('Area')).
maclan(tag('UNZIP'), property('res'), type('String')).
maclan(tag('UNZIP'), property('folder'), type('FolderPath')).

maclan(tag('RUN'), property('cmd'), type('SystemCommand')).
maclan(tag('RUN'), property('output'), type('Void')).
maclan(tag('RUN'), property('exception'), type('Void')).

maclan(tag('REDIRECT'), property('uri'), type('Uri')).

maclan(tag('TRAY_MENU'), property('name'), type('String')).
maclan(tag('TRAY_MENU'), property('url'), type('Url')).
