-- ============================================================================
-- === Comment ...
-- ============================================================================

CREATE TABLE properties											\
(																				\
	name	VARCHAR_IGNORECASE PRIMARY KEY,							\
	value	VARCHAR															\
);

INSERT INTO properties VALUES( 'database.created', CURRENT_TIMESTAMP() );
INSERT INTO properties VALUES( 'database.user', CURRENT_USER() );
