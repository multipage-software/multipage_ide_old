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
maclan(tag('TIMEOUT'), type('Simple')).
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
maclan(tag('AREA_NAME'), property('areaSlot'), type('SlotAlias')).

maclan(tag('AREA_ID'), property('areaId'), type('AreaId')).
maclan(tag('AREA_ID'), property('areaAlias'), type('AreaAlias')).
maclan(tag('AREA_ID'), property('area'), type('Area')).
maclan(tag('AREA_ID'), property('startArea'), type('Area')).
maclan(tag('AREA_ID'), property('homeArea'), type('Area')).
maclan(tag('AREA_ID'), property('requestedArea'), type('Area')).
maclan(tag('AREA_ID'), property('thisArea'), type('Area')).
maclan(tag('AREA_ID'), property('areaSlot'), type('SlotAlias')).

maclan(tag('AREA_ALIAS'), property('areaId'), type('AreaId')).
maclan(tag('AREA_ALIAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('AREA_ALIAS'), property('area'), type('Area')).
maclan(tag('AREA_ALIAS'), property('startArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('homeArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('requestedArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('thisArea'), type('Area')).
maclan(tag('AREA_ALIAS'), property('areaSlot'), type('SlotAlias')).

maclan(tag('SUBAREAS'), property('list'), type('VarName')).
maclan(tag('SUBAREAS'), property('first'), type('VarName')).
maclan(tag('SUBAREAS'), property('last'), type('VarName')).
maclan(tag('SUBAREAS'), property('cond'), type('Boolean')).
maclan(tag('SUBAREAS'), property('reversed'), type('Boolean')).
maclan(tag('SUBAREAS'), property('areaId'), type('AreaId')).
maclan(tag('SUBAREAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('SUBAREAS'), property('area'), type('Area')).
maclan(tag('SUBAREAS'), property('startArea'), type('Area')).
maclan(tag('SUBAREAS'), property('homeArea'), type('Area')).
maclan(tag('SUBAREAS'), property('requestedArea'), type('Area')).
maclan(tag('SUBAREAS'), property('thisArea'), type('Area')).
maclan(tag('SUBAREAS'), property('areaSlot'), type('SlotAlias')).

maclan(tag('SUPERAREAS'), property('list'), type('VarName')).
maclan(tag('SUPERAREAS'), property('first'), type('VarName')).
maclan(tag('SUPERAREAS'), property('last'), type('VarName')).
maclan(tag('SUPERAREAS'), property('cond'), type('Boolean')).
maclan(tag('SUPERAREAS'), property('reversed'), type('Boolean')).
maclan(tag('SUPERAREAS'), property('areaId'), type('AreaId')).
maclan(tag('SUPERAREAS'), property('areaAlias'), type('AreaAlias')).
maclan(tag('SUPERAREAS'), property('area'), type('Area')).
maclan(tag('SUPERAREAS'), property('startArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('homeArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('requestedArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('thisArea'), type('Area')).
maclan(tag('SUPERAREAS'), property('areaSlot'), type('SlotAlias')).

maclan(tag('TAG'), property('slot'), type('SlotAlias')).
maclan(tag('TAG'), property('local'), type('Boolean')).
maclan(tag('TAG'), property('skipDefault'), type('Boolean')).
maclan(tag('TAG'), property('parent'), type('Boolean')).
maclan(tag('TAG'), property('enableSpecialValue'), type('Boolean')).
maclan(tag('TAG'), property('areaId'), type('AreaId')).
maclan(tag('TAG'), property('areaAlias'), type('AreaAlias')).
maclan(tag('TAG'), property('area'), type('Area')).
maclan(tag('TAG'), property('startArea'), type('Area')).
maclan(tag('TAG'), property('homeArea'), type('Area')).
maclan(tag('TAG'), property('requestedArea'), type('Area')).
maclan(tag('TAG'), property('thisArea'), type('Area')).
maclan(tag('TAG'), property('areaSlot'), type('SlotAlias')).

maclan(tag('LANGUAGES'), property('divider'), type('String')).
maclan(tag('LANGUAGES'), property('transparent'), type('Boolean')).
maclan(tag('LANGUAGES'), property('transparentProc'), type('Boolean')).
maclan(tag('LANGUAGES'), property('transparentVar'), type('Boolean')).

maclan(tag('RESOURCE_ID'), property('res'), type('ResourceAlias')).
maclan(tag('RESOURCE_ID'), property('render'), type('Boolean')).
maclan(tag('RESOURCE_ID'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_ID'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_ID'), property('area'), type('Area')).
maclan(tag('RESOURCE_ID'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_ID'), property('areaSlot'), type('SlotAlias')).

maclan(tag('RESOURCE_EXT'), property('res'), type('ResourceAlias')).
maclan(tag('RESOURCE_EXT'), property('render'), type('Boolean')).
maclan(tag('RESOURCE_EXT'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_EXT'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_EXT'), property('area'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_EXT'), property('areaSlot'), type('SlotAlias')).

maclan(tag('RESOURCE_VALUE'), property('res'), type('ResourceAlias')).
maclan(tag('RESOURCE_VALUE'), property('render'), type('Boolean')).
maclan(tag('RESOURCE_VALUE'), property('encoding'), type('CodeAlias')).
maclan(tag('RESOURCE_VALUE'), property('areaId'), type('AreaId')).
maclan(tag('RESOURCE_VALUE'), property('areaAlias'), type('AreaAlias')).
maclan(tag('RESOURCE_VALUE'), property('area'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('startArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('homeArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('requestedArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('thisArea'), type('Area')).
maclan(tag('RESOURCE_VALUE'), property('areaSlot'), type('SlotAlias')).

maclan(tag('VERSION_ANCHOR'), property('versionAlias'), type('VersionAlias')).
maclan(tag('VERSION_ANCHOR'), property('areaId'), type('AreaId')).
maclan(tag('VERSION_ANCHOR'), property('areaAlias'), type('AreaAlias')).
maclan(tag('VERSION_ANCHOR'), property('area'), type('Area')).
maclan(tag('VERSION_ANCHOR'), property('startArea'), type('Area')).
maclan(tag('VERSION_ANCHOR'), property('homeArea'), type('Area')).
maclan(tag('VERSION_ANCHOR'), property('requestedArea'), type('Area')).
maclan(tag('VERSION_ANCHOR'), property('thisArea'), type('Area')).
maclan(tag('VERSION_ANCHOR'), property('areaSlot'), type('SlotAlias')).

maclan(tag('VERSION_URL'), property('versionAlias'), type('VersionAlias')).
maclan(tag('VERSION_URL'), property('areaId'), type('AreaId')).
maclan(tag('VERSION_URL'), property('areaAlias'), type('AreaAlias')).
maclan(tag('VERSION_URL'), property('area'), type('Area')).
maclan(tag('VERSION_URL'), property('startArea'), type('Area')).
maclan(tag('VERSION_URL'), property('homeArea'), type('Area')).
maclan(tag('VERSION_URL'), property('requestedArea'), type('Area')).
maclan(tag('VERSION_URL'), property('thisArea'), type('Area')).
maclan(tag('VERSION_URL'), property('areaSlot'), type('SlotAlias')).

maclan(tag('VERSION_NAME'), property('versionAlias'), type('VersionAlias')).
maclan(tag('VERSION_NAME'), property('areaId'), type('AreaId')).
maclan(tag('VERSION_NAME'), property('areaAlias'), type('AreaAlias')).
maclan(tag('VERSION_NAME'), property('area'), type('Area')).
maclan(tag('VERSION_NAME'), property('startArea'), type('Area')).
maclan(tag('VERSION_NAME'), property('homeArea'), type('Area')).
maclan(tag('VERSION_NAME'), property('requestedArea'), type('Area')).
maclan(tag('VERSION_NAME'), property('thisArea'), type('Area')).
maclan(tag('VERSION_NAME'), property('areaSlot'), type('SlotAlias')).

maclan(tag('VERSION_ID'), property('versionAlias'), type('VersionAlias')).
maclan(tag('VERSION_ID'), property('areaId'), type('AreaId')).
maclan(tag('VERSION_ID'), property('areaAlias'), type('AreaAlias')).
maclan(tag('VERSION_ID'), property('area'), type('Area')).
maclan(tag('VERSION_ID'), property('startArea'), type('Area')).
maclan(tag('VERSION_ID'), property('homeArea'), type('Area')).
maclan(tag('VERSION_ID'), property('requestedArea'), type('Area')).
maclan(tag('VERSION_ID'), property('thisArea'), type('Area')).
maclan(tag('VERSION_ID'), property('areaSlot'), type('SlotAlias')).

maclan(tag('LIST'), property('list'), type('Collection')).
maclan(tag('LIST'), property('iterator'), type('VarName')).
maclan(tag('LIST'), property('item'), type('VarName')).
maclan(tag('LIST'), property('divider'), type('String')).
maclan(tag('LIST'), property('local'), type('Boolean')).
maclan(tag('LIST'), property('break'), type('VarName')).
maclan(tag('LIST'), property('discard'), type('VarName')).
maclan(tag('LIST'), property('transparent'), type('Boolean')).
maclan(tag('LIST'), property('transparentProc'), type('Boolean')).
maclan(tag('LIST'), property('transparentVar'), type('Boolean')).

maclan(tag('LAST'), property('discard'), type('Boolean')).

maclan(tag('LOOP'), property('count'), type('Long')).
maclan(tag('LOOP'), property('divider'), type('String')).
maclan(tag('LOOP'), property('index'), type('Long')).
maclan(tag('LOOP'), property('break'), type('Boolean')).
maclan(tag('LOOP'), property('discard'), type('Boolean')).
maclan(tag('LOOP'), property('from'), type('Long')).
maclan(tag('LOOP'), property('to'), type('Long')).
maclan(tag('LOOP'), property('step'), type('Long')).
maclan(tag('LOOP'), property('transparent'), type('Boolean')).
maclan(tag('LOOP'), property('transparentProc'), type('Boolean')).
maclan(tag('LOOP'), property('transparentVar'), type('Boolean')).

maclan(tag('IMAGE'), property('res'), type('ResourceAlias')).
maclan(tag('IMAGE'), property('areaId'), type('AreaId')).
maclan(tag('IMAGE'), property('areaAlias'), type('AreaAlias')).
maclan(tag('IMAGE'), property('area'), type('Area')).
maclan(tag('IMAGE'), property('startArea'), type('Area')).
maclan(tag('IMAGE'), property('homeArea'), type('Area')).
maclan(tag('IMAGE'), property('requestedArea'), type('Area')).
maclan(tag('IMAGE'), property('thisArea'), type('Area')).
maclan(tag('IMAGE'), property('areaSlot'), type('SlotAlias')).

maclan(tag('GET'), property('exp'), type('Expression')).

maclan(tag('A'), property('href'), type('String')).
maclan(tag('A'), property('areaId'), type('AreaId')).
maclan(tag('A'), property('areaAlias'), type('AreaAlias')).
maclan(tag('A'), property('area'), type('Area')).
maclan(tag('A'), property('startArea'), type('Area')).
maclan(tag('A'), property('homeArea'), type('Area')).
maclan(tag('A'), property('requestedArea'), type('Area')).
maclan(tag('A'), property('thisArea'), type('Area')).
maclan(tag('A'), property('areaSlot'), type('SlotAlias')).

maclan(tag('ANCHOR'), property('href'), type('String')).
maclan(tag('ANCHOR'), property('areaId'), type('AreaId')).
maclan(tag('ANCHOR'), property('areaAlias'), type('AreaAlias')).
maclan(tag('ANCHOR'), property('area'), type('Area')).
maclan(tag('ANCHOR'), property('startArea'), type('Area')).
maclan(tag('ANCHOR'), property('homeArea'), type('Area')).
maclan(tag('ANCHOR'), property('requestedArea'), type('Area')).
maclan(tag('ANCHOR'), property('thisArea'), type('Area')).
maclan(tag('ANCHOR'), property('areaSlot'), type('SlotAlias')).

maclan(tag('IF'), property('cond'), type('Boolean')).
maclan(tag('ELSEIF'), property('cond'), type('Boolean')).

maclan(tag('PROCEDURE'), property('name'), type('ProcedureName')).
maclan(tag('PROCEDURE'), property('$name'), type('ProcedureName')).
maclan(tag('PROCEDURE'), property('$useLast'), type('Boolean')).
maclan(tag('PROCEDURE'), property('$global'), type('Boolean')).
maclan(tag('PROCEDURE'), property('$returnText'), type('Boolean')).
maclan(tag('PROCEDURE'), property('$inner'), type('Boolean')).
maclan(tag('PROCEDURE'), property('transparent'), type('Boolean')).
maclan(tag('PROCEDURE'), property('transparentProc'), type('Boolean')).
maclan(tag('PROCEDURE'), property('transparentVar'), type('Boolean')).

maclan(tag('CALL'), property('name'), type('Procedure')).
maclan(tag('CALL'), property('$name'), type('Procedure')).
maclan(tag('CALL'), property('$areaId'), type('Long')).
maclan(tag('CALL'), property('$areaAlias'), type('String')).
maclan(tag('CALL'), property('$area'), type('Area')).
maclan(tag('CALL'), property('$startArea'), type('Area')).
maclan(tag('CALL'), property('$homeArea'), type('Area')).
maclan(tag('CALL'), property('$requestedArea'), type('Area')).
maclan(tag('CALL'), property('$thisArea'), type('Area')).
maclan(tag('CALL'), property('$areaSlot'), type('SlotAlias')).
maclan(tag('CALL'), property('$parent'), type('Boolean')).
maclan(tag('CALL'), property('$inner'), type('Boolean')).
maclan(tag('CALL'), property('transparent'), type('Boolean')).
maclan(tag('CALL'), property('transparentProc'), type('Boolean')).
maclan(tag('CALL'), property('transparentVar'), type('Boolean')).

maclan(tag('PACK'), property('strong'), type('Boolean')).
maclan(tag('PACK'), property('trim'), type('Boolean')).
maclan(tag('PACK'), property('trimBegin'), type('Boolean')).
maclan(tag('PACK'), property('trimEnd'), type('Boolean')).
maclan(tag('PACK'), property('transparent'), type('Boolean')).
maclan(tag('PACK'), property('transparentProc'), type('Boolean')).
maclan(tag('PACK'), property('transparentVar'), type('Boolean')).

maclan(tag('BLOCK'), property('areaId'), type('AreaId')).
maclan(tag('BLOCK'), property('areaAlias'), type('AreaAlias')).
maclan(tag('BLOCK'), property('area'), type('Area')).
maclan(tag('BLOCK'), property('startArea'), type('Area')).
maclan(tag('BLOCK'), property('homeArea'), type('Area')).
maclan(tag('BLOCK'), property('requestedArea'), type('Area')).
maclan(tag('BLOCK'), property('thisArea'), type('Area')).
maclan(tag('BLOCK'), property('areaSlot'), type('SlotAlias')).
maclan(tag('BLOCK'), property('transparent'), type('Boolean')).
maclan(tag('BLOCK'), property('transparentProc'), type('Boolean')).
maclan(tag('BLOCK'), property('transparentVar'), type('Boolean')).

maclan(tag('OUTPUT'), property('transparent'), type('Boolean')).
maclan(tag('OUTPUT'), property('transparentProc'), type('Boolean')).
maclan(tag('OUTPUT'), property('transparentVar'), type('Boolean')).

maclan(tag('TRACE'), property('name'), type('String')).
maclan(tag('TRACE'), property('simple'), type('Boolean')).

maclan(tag('BREAK'), property('name'), type('String')).
maclan(tag('BREAK'), property('no'), type('Boolean')).

maclan(tag('URL'), property('areaId'), type('AreaId')).
maclan(tag('URL'), property('areaAlias'), type('AreaAlias')).
maclan(tag('URL'), property('area'), type('Area')).
maclan(tag('URL'), property('startArea'), type('Area')).
maclan(tag('URL'), property('homeArea'), type('Area')).
maclan(tag('URL'), property('requestedArea'), type('Area')).
maclan(tag('URL'), property('thisArea'), type('Area')).
maclan(tag('URL'), property('areaSlot'), type('SlotAlias')).
maclan(tag('URL'), property('res'), type('ResourceAlias')).
maclan(tag('URL'), property('localhost'), type('LocalHost')).
maclan(tag('URL'), property('langAlias'), type('String')).
maclan(tag('URL'), property('versionAlias'), type('String')).
maclan(tag('URL'), property('download'), type('Boolean')).
maclan(tag('URL'), property('file'), type('String')).

maclan(tag('USING'), property('res'), type('ResourceAlias')).
maclan(tag('USING'), property('resId'), type('Long')).
maclan(tag('USING'), property('file'), type('String')).
maclan(tag('USING'), property('extract'), type('Boolean')).
maclan(tag('USING'), property('encoding'), type('CodeAlias')).
maclan(tag('USING'), property('areaId'), type('AreaId')).
maclan(tag('USING'), property('areaAlias'), type('AreaAlias')).
maclan(tag('USING'), property('area'), type('Area')).
maclan(tag('USING'), property('startArea'), type('Area')).
maclan(tag('USING'), property('homeArea'), type('Area')).
maclan(tag('USING'), property('requestedArea'), type('Area')).
maclan(tag('USING'), property('thisArea'), type('Area')).
maclan(tag('USING'), property('areaSlot'), type('SlotAlias')).

maclan(tag('RENDER_CLASS'), property('name'), type('RenderedClass')).
maclan(tag('RENDER_CLASS'), property('text'), type('String')).

maclan(tag('TIMEOUT'), property('ms'), type('Long')).

maclan(tag('BOOKMARK'), property('name'), type('BookmarkName')).

maclan(tag('REPLACE_BOOKMARK'), property('name'), type('BookmarkName')).
maclan(tag('REPLACE_BOOKMARK'), property('transparent'), type('Boolean')).
maclan(tag('REPLACE_BOOKMARK'), property('transparentProc'), type('Boolean')).
maclan(tag('REPLACE_BOOKMARK'), property('transparentVar'), type('Boolean')).

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
maclan(tag('UNZIP'), property('res'), type('ResourceAlias')).
maclan(tag('UNZIP'), property('folder'), type('FolderPath')).

maclan(tag('RUN'), property('cmd'), type('SystemCommand')).
maclan(tag('RUN'), property('output'), type('Boolean')).
maclan(tag('RUN'), property('exception'), type('Void')).

maclan(tag('REDIRECT'), property('uri'), type('Uri')).

maclan(tag('TRAY_MENU'), property('name'), type('String')).
maclan(tag('TRAY_MENU'), property('url'), type('Url')).
