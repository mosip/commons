
CREATE SCHEMA IF NOT EXISTS iam;

CREATE TABLE IF NOT EXISTS iam.oauth_access_token(
	auth_token character varying(1024),
	user_id character varying(256) NOT NULL,
	refresh_token character varying(1024),
	expiration_time timestamp,
	is_active boolean NOT NULL,
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	constraint pk_authat_id primary key (user_id)
);
