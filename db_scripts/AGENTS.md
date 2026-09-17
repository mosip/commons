# db_scripts/

Greenfield only (`deploy.sh` **drops** DB/role). Schemas: `mosip_kernel` · `mosip_authdevice` · `mosip_regdevice`. New table: `ddl/` + `\ir` in `ddl.sql`. Kernel DDL → also upgrade + release SQL.

`cd db_scripts/<schema> && ./deploy.sh deploy.properties` (set `SU_USER_PWD`, `DBUSER_PWD`). Prefer per-schema `deploy.sh` over `mosip_commons_db_deployment.sh`.
