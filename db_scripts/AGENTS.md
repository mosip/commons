# db_scripts/

Greenfield (`deploy.sh` **drops** DB/role). Schemas: `mosip_kernel` · `mosip_authdevice` · `mosip_regdevice`. New table → `ddl/` + `\ir` in `ddl.sql` → also upgrade+release.

`cd db_scripts/<schema> && ./deploy.sh deploy.properties`. Prefer per-schema over `mosip_commons_db_deployment.sh`.
