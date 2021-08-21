CREATE OR REPLACE FUNCTION get_lo_size(oid oid)
  RETURNS bigint AS
$BODY$DECLARE
    fd integer;
    sz bigint;
BEGIN
    fd := lo_open($1, x'40000'::int);
    PERFORM lo_lseek(fd, 0, 2);
    sz := lo_tell(fd);
    PERFORM lo_close(fd);
    RETURN sz;
END;$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100;
  
-- DIVIDER

CREATE OR REPLACE FUNCTION get_localized_text(
    _text_id bigint,
    _language_id bigint)
  RETURNS text AS
$BODY$
  DECLARE local_text text;
  BEGIN
	SELECT text INTO local_text FROM localized_text WHERE text_id = _text_id AND language_id = _language_id;
	IF local_text IS NOT NULL THEN
		RETURN local_text;
	ELSE
		local_text = NULL;
		SELECT text INTO local_text FROM localized_text WHERE text_id = _text_id AND language_id = 0;
		RETURN local_text;
	END IF;
  END;
  $BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
-- DIVIDER

CREATE OR REPLACE FUNCTION getunknwnstr()
  RETURNS text AS
$BODY$
BEGIN
	RETURN 'UNKNOWN';
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
  
-- DIVIDER
  
CREATE SEQUENCE area_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 12102
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE area_resource_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 912
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE area_slot_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 32243
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE constructor_group_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 301
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE constructor_holder_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 422
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE description_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1619
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE enumeration_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 18
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE enumeration_value_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 126
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE is_subarea_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 12603
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE language_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 90
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE mime_type_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 39684
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE namespace_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 248
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE resource_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1620
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE text_id_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 26642
  CACHE 1;
  
-- DIVIDER
  
CREATE SEQUENCE version_id
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 10
  CACHE 1;
  
-- DIVIDER

CREATE TABLE enumeration_value (
		enumeration_id INT8 NOT NULL,
		id BIGINT DEFAULT nextval('enumeration_value_id'::regclass) NOT NULL,
		enum_value TEXT NOT NULL,
		description TEXT
	);-- DIVIDER

CREATE TABLE version (
		id BIGINT DEFAULT nextval('version_id'::regclass) NOT NULL,
		alias TEXT,
		description_id INT8
	);-- DIVIDER

CREATE TABLE mime_type (
		id BIGINT DEFAULT nextval('mime_type_id'::regclass) NOT NULL,
		extension TEXT NOT NULL,
		type TEXT NOT NULL,
		preference BOOL DEFAULT false NOT NULL
	);-- DIVIDER

CREATE TABLE language (
		id BIGINT DEFAULT nextval('language_id'::regclass) NOT NULL,
		description TEXT NOT NULL,
		alias TEXT NOT NULL,
		icon BYTEA,
		priority INT4 DEFAULT 0 NOT NULL
	);-- DIVIDER

CREATE TABLE area_slot (
		alias TEXT NOT NULL,
		area_id INT8 NOT NULL,
		revision INT8 DEFAULT 0 NOT NULL,
		created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
		localized_text_value_id INT8,
		text_value TEXT,
		integer_value INT8,
		real_value FLOAT8,
		id BIGINT DEFAULT nextval('area_slot_id'::regclass) NOT NULL,
		access BPCHAR(1) DEFAULT 'T'::bpchar NOT NULL,
		hidden BOOL DEFAULT false NOT NULL,
		boolean_value BOOL,
		enumeration_value_id INT8,
		color INT8,
		description_id INT8,
		is_default BOOL DEFAULT false NOT NULL,
		name TEXT,
		value_meaning BPCHAR(3),
		user_defined BOOL,
		preferred BOOL,
		special_value TEXT,
		area_value INT8,
		external_provider TEXT,
    external_change BOOL,
		reads_input BOOL,
    input_lock BOOL,
		writes_output BOOL,
    output_lock BOOL,
		output_text TEXT,
		exposed BOOL DEFAULT false
	);-- DIVIDER

CREATE TABLE enumeration (
		id BIGINT DEFAULT nextval('enumeration_id'::regclass) NOT NULL,
		description TEXT NOT NULL
	);-- DIVIDER

CREATE TABLE start_area (
		area_id INT8 NOT NULL
	);-- DIVIDER

CREATE TABLE area (
		id BIGINT DEFAULT nextval('area_id'::regclass) NOT NULL,
    guid uuid,
		start_resource INT8,
		description_id INT8,
		visible BOOL DEFAULT true NOT NULL,
		alias TEXT,
		read_only BOOL DEFAULT true NOT NULL,
		help TEXT,
		localized BOOL DEFAULT true NOT NULL,
		filename TEXT,
		version_id INT8,
		folder TEXT,
		start_resource_not_localized BOOL,
		constructors_group_id INT8,
		related_area_id INT8,
		constructor_holder_id INT8,
		file_extension TEXT,
		can_import BOOL,
		project_root BOOL,
		enabled BOOL DEFAULT true
	);-- DIVIDER

CREATE TABLE text_id (
		id BIGINT DEFAULT nextval('text_id_id'::regclass) NOT NULL
	);-- DIVIDER

CREATE TABLE localized_text (
		text_id INT8 NOT NULL,
		language_id INT8 NOT NULL,
		text TEXT
	);-- DIVIDER

CREATE TABLE area_resource (
		area_id INT8 NOT NULL,
		resource_id INT8 NOT NULL,
		local_description TEXT DEFAULT ''::text NOT NULL,
		id BIGINT DEFAULT nextval('area_resource_id'::regclass) NOT NULL
	);-- DIVIDER

CREATE TABLE description (
		id BIGINT DEFAULT nextval('description_id'::regclass) NOT NULL,
		description TEXT NOT NULL
	);-- DIVIDER

CREATE TABLE resource (
		id BIGINT DEFAULT nextval('resource_id'::regclass) NOT NULL,
		namespace_id INT8 DEFAULT 0 NOT NULL,
		description TEXT,
		mime_type_id INT8 DEFAULT 0 NOT NULL,
		visible BOOL,
		blob OID,
		text TEXT,
		protected BOOL DEFAULT false NOT NULL
	);-- DIVIDER

CREATE TABLE is_subarea (
		area_id INT8 NOT NULL,
		subarea_id INT8 NOT NULL,
		inheritance BOOL DEFAULT false NOT NULL,
		id BIGINT DEFAULT nextval('is_subarea_id'::regclass),
		priority_sub INT4 DEFAULT 0 NOT NULL,
		priority_super INT4 DEFAULT 0 NOT NULL,
		name_sub TEXT,
		name_super TEXT,
		hide_sub BOOL DEFAULT false NOT NULL,
		recursion BOOL DEFAULT false NOT NULL
	);-- DIVIDER

CREATE TABLE constructor_group (
		id BIGINT DEFAULT nextval('constructor_group_id'::regclass) NOT NULL,
		extension_area_id INT8,
		alias TEXT
	);-- DIVIDER

CREATE TABLE constructor_holder (
		id BIGINT DEFAULT nextval('constructor_holder_id'::regclass) NOT NULL,
		area_id INT8 NOT NULL,
		group_id INT8 NOT NULL,
		subgroup_id INT8,
		name TEXT NOT NULL,
		inheritance BOOL DEFAULT false NOT NULL,
		sub_relation_name TEXT,
		super_relation_name TEXT,
		is_sub_reference BOOL,
		ask_related_area BOOL DEFAULT false NOT NULL,
		subgroup_aliases TEXT,
		invisible BOOL DEFAULT false,
		alias TEXT,
		set_home BOOL,
		constructor_link BIGINT
	);-- DIVIDER

CREATE TABLE area_sources (
		area_id INT8 NOT NULL,
		resource_id INT8 NOT NULL,
		version_id INT8 NOT NULL,
		not_localized BOOL DEFAULT false NOT NULL
	);-- DIVIDER

CREATE TABLE namespace (
		id BIGINT DEFAULT nextval('namespace_id'::regclass) NOT NULL,
		description TEXT DEFAULT getunknwnstr() NOT NULL,
		parent_id INT8 NOT NULL
	);-- DIVIDER

CREATE TABLE start_language (
		language_id INT8 NOT NULL
	);-- DIVIDER
	


CREATE INDEX fki_constructor_holder_subgroup_id_fkey ON constructor_holder (subgroup_id ASC);-- DIVIDER

CREATE INDEX fki_area_description_id_fkey ON area (description_id ASC);-- DIVIDER

CREATE INDEX fki_area_slot_area_id_fkey ON area_slot (area_id ASC);-- DIVIDER

CREATE INDEX fki_version_description_id_fkey ON version (description_id ASC);-- DIVIDER

CREATE INDEX fki_area_slot_text_value_id_fkey ON area_slot (localized_text_value_id ASC);-- DIVIDER

CREATE UNIQUE INDEX mime_type_unique ON mime_type (id ASC);-- DIVIDER

CREATE INDEX fki_area_slot_description_id_fkey ON area_slot (description_id ASC);-- DIVIDER

CREATE INDEX fki_area_version_id_fkey ON area (version_id ASC);-- DIVIDER

CREATE INDEX area_slot_area_id_is_default_index ON area_slot (area_id ASC, is_default ASC);-- DIVIDER

CREATE INDEX fki_area_related_area_id_fkey ON area (related_area_id ASC);-- DIVIDER

CREATE INDEX localized_text_language_id ON localized_text (language_id ASC);-- DIVIDER

CREATE INDEX fki_constructor_holder_area_id_fkey ON constructor_holder (area_id ASC);-- DIVIDER

CREATE INDEX fki_constructor_group_extension_area_id_fkey ON constructor_group (extension_area_id ASC);-- DIVIDER

CREATE UNIQUE INDEX version_alias_unique ON version (alias ASC);-- DIVIDER

CREATE INDEX fki_area_constructors_group_id_fkey ON area (constructors_group_id ASC);-- DIVIDER

CREATE INDEX fki_start_area_area_id_fkey ON start_area (area_id ASC);-- DIVIDER

CREATE INDEX pki_area_sources ON area_sources (area_id ASC, resource_id ASC, version_id ASC);-- DIVIDER

CREATE INDEX language_id_index ON language (id ASC);-- DIVIDER

CREATE INDEX area_index ON area (id ASC);-- DIVIDER

CREATE INDEX localized_text_id ON localized_text (text_id ASC);-- DIVIDER

CREATE UNIQUE INDEX namespace_id_unique ON namespace (id ASC);-- DIVIDER

CREATE UNIQUE INDEX enumeration_id_unique ON enumeration (id ASC);-- DIVIDER

CREATE UNIQUE INDEX enumeration_values_id_unique ON enumeration_value (id ASC);-- DIVIDER

CREATE INDEX fki_area_sources_area_id_fkey ON area_sources (area_id ASC);-- DIVIDER

CREATE INDEX fki_area_slot_enumeration_value_id_fkey ON area_slot (enumeration_value_id ASC);-- DIVIDER

CREATE INDEX is_subarea_index1 ON is_subarea (area_id ASC, subarea_id ASC);-- DIVIDER

CREATE INDEX fki_area_sources_version_id_fkey ON area_sources (version_id ASC);-- DIVIDER

CREATE INDEX fki_constructor_holder_group_id_fkey ON constructor_holder (group_id ASC);-- DIVIDER

CREATE INDEX fki_area_slot_area_value_fkey ON area_slot (area_value ASC);-- DIVIDER

CREATE INDEX fki_area_constructor_holder_id_fkey ON area (constructor_holder_id ASC);-- DIVIDER

CREATE UNIQUE INDEX area_resource_id_unique ON area_resource (id ASC);-- DIVIDER

CREATE INDEX fki_area_sources_resource_id_fkey ON area_sources (resource_id ASC);-- DIVIDER

CREATE INDEX area_slot_index ON area_slot (alias ASC, area_id ASC);-- DIVIDER

ALTER TABLE mime_type ADD CONSTRAINT mime_type_pkey PRIMARY KEY (extension, type);-- DIVIDER

ALTER TABLE constructor_holder ADD CONSTRAINT constructor_holder_id_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE language ADD CONSTRAINT language_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE enumeration ADD CONSTRAINT enumeration_description_pkey PRIMARY KEY (description);-- DIVIDER

ALTER TABLE text_id ADD CONSTRAINT text_id_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE description ADD CONSTRAINT description_id_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE enumeration_value ADD CONSTRAINT enumeration_values_pkey PRIMARY KEY (enumeration_id, enum_value);-- DIVIDER

ALTER TABLE is_subarea ADD CONSTRAINT is_subarea_pkey PRIMARY KEY (area_id, subarea_id);-- DIVIDER

ALTER TABLE area_resource ADD CONSTRAINT area_resource_pkey PRIMARY KEY (area_id, resource_id, local_description);-- DIVIDER

ALTER TABLE constructor_group ADD CONSTRAINT constructor_group_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE start_area ADD CONSTRAINT start_area_pkey PRIMARY KEY (area_id);-- DIVIDER

ALTER TABLE area_sources ADD CONSTRAINT area_sources_pkey PRIMARY KEY (area_id, resource_id, version_id);-- DIVIDER

ALTER TABLE localized_text ADD CONSTRAINT localized_text_pkey PRIMARY KEY (text_id, language_id);-- DIVIDER

ALTER TABLE start_language ADD CONSTRAINT start_language_pkey PRIMARY KEY (language_id);-- DIVIDER

ALTER TABLE resource ADD CONSTRAINT resource_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_id_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE version ADD CONSTRAINT version_id_pkey PRIMARY KEY (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_pkey PRIMARY KEY (alias, area_id, revision);-- DIVIDER

ALTER TABLE namespace ADD CONSTRAINT namespace_pkey PRIMARY KEY (description, parent_id);-- DIVIDER

ALTER TABLE area_resource ADD CONSTRAINT area_resource_r_fkey FOREIGN KEY (resource_id)
	REFERENCES resource (id);-- DIVIDER

ALTER TABLE start_area ADD CONSTRAINT start_area_area_id_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE resource ADD CONSTRAINT resource_mime_fkey FOREIGN KEY (mime_type_id)
	REFERENCES mime_type (id);-- DIVIDER

ALTER TABLE localized_text ADD CONSTRAINT localized_text_language_id_fkey FOREIGN KEY (language_id)
	REFERENCES language (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_text_value_id_fkey FOREIGN KEY (localized_text_value_id)
	REFERENCES text_id (id);-- DIVIDER

ALTER TABLE version ADD CONSTRAINT version_description_id_fkey FOREIGN KEY (description_id)
	REFERENCES text_id (id);-- DIVIDER

ALTER TABLE area_sources ADD CONSTRAINT area_sources_resource_id_fkey FOREIGN KEY (resource_id)
	REFERENCES resource (id);-- DIVIDER

ALTER TABLE constructor_group ADD CONSTRAINT constructor_group_extension_area_id_fkey FOREIGN KEY (extension_area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_description_id_fkey FOREIGN KEY (description_id)
	REFERENCES description (id);-- DIVIDER

ALTER TABLE constructor_holder ADD CONSTRAINT constructor_holder_subgroup_id_fkey FOREIGN KEY (subgroup_id)
	REFERENCES constructor_group (id);-- DIVIDER

ALTER TABLE area_resource ADD CONSTRAINT area_resource_a_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_description_id_fkey FOREIGN KEY (description_id)
	REFERENCES text_id (id);-- DIVIDER

ALTER TABLE enumeration_value ADD CONSTRAINT enumeration_values_id_fkey FOREIGN KEY (enumeration_id)
	REFERENCES enumeration (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_area_id_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_enumeration_value_id_fkey FOREIGN KEY (enumeration_value_id)
	REFERENCES enumeration_value (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_start_resource_fkey FOREIGN KEY (start_resource)
	REFERENCES resource (id);-- DIVIDER

ALTER TABLE is_subarea ADD CONSTRAINT is_subarea_subarea_id_fkey FOREIGN KEY (subarea_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE area_sources ADD CONSTRAINT area_sources_version_id_fkey FOREIGN KEY (version_id)
	REFERENCES version (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_constructor_holder_id_fkey FOREIGN KEY (constructor_holder_id)
	REFERENCES constructor_holder (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_constructors_group_id_fkey FOREIGN KEY (constructors_group_id)
	REFERENCES constructor_group (id);-- DIVIDER

ALTER TABLE area_slot ADD CONSTRAINT area_slot_area_value_fkey FOREIGN KEY (area_value)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE namespace ADD CONSTRAINT namespace_parent_id_fkey FOREIGN KEY (parent_id)
	REFERENCES namespace (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_version_id_fkey FOREIGN KEY (version_id)
	REFERENCES version (id);-- DIVIDER

ALTER TABLE area_sources ADD CONSTRAINT area_sources_area_id_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE start_language ADD CONSTRAINT start_laguage_language_id_fkey FOREIGN KEY (language_id)
	REFERENCES language (id);-- DIVIDER

ALTER TABLE resource ADD CONSTRAINT resource_fkey FOREIGN KEY (namespace_id)
	REFERENCES namespace (id);-- DIVIDER

ALTER TABLE constructor_holder ADD CONSTRAINT constructor_holder_group_id_fkey FOREIGN KEY (group_id)
	REFERENCES constructor_group (id);-- DIVIDER

ALTER TABLE is_subarea ADD CONSTRAINT is_subarea_area_id_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE constructor_holder ADD CONSTRAINT constructor_holder_area_id_fkey FOREIGN KEY (area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE area ADD CONSTRAINT area_related_area_id_fkey FOREIGN KEY (related_area_id)
	REFERENCES area (id);-- DIVIDER

ALTER TABLE localized_text ADD CONSTRAINT localized_text_text_id_fkey FOREIGN KEY (text_id)
	REFERENCES text_id (id);

ALTER TABLE constructor_holder ADD CONSTRAINT conctructor_holder_link_fkey FOREIGN KEY (constructor_link)
      REFERENCES constructor_holder (id);
      
ALTER TABLE constructor_holder ADD CONSTRAINT constructor_holder_link_check CHECK (NOT constructor_link = id);

-- DIVIDER

INSERT INTO text_id (id) VALUES (0);

-- DIVIDER

INSERT INTO text_id (id) VALUES (1);

-- DIVIDER

INSERT INTO language (id, description, alias, priority) VALUES (0, 'Èeština', 'cz', 0);

-- DIVIDER

INSERT INTO localized_text (text_id, language_id, text) VALUES (0, 0, 'Global Area');

-- DIVIDER

INSERT INTO localized_text (text_id, language_id, text) VALUES (1, 0, 'Default');

-- DIVIDER

INSERT INTO version (id, alias, description_id) VALUES (0, 'def', 1);

-- DIVIDER

INSERT INTO namespace (id, description, parent_id) VALUES (0, '/', 0);

-- DIVIDER

INSERT INTO start_language (language_id) VALUES (0);

-- DIVIDER

INSERT INTO area (id, description_id, visible, read_only, localized, can_import) VALUES (0, 0, FALSE, TRUE, FALSE, TRUE);

-- DIVIDER

INSERT INTO start_area (area_id) VALUES (0);
