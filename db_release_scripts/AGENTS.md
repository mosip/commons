# db_release_scripts/

`mosip_kernel/sql/{version}-release.sql` + `{version}-revoke.sql`. Always ship revoke.

`cd db_release_scripts/mosip_kernel && ./deploy.sh deploy.properties <version>` (`revoke.sh` to undo). Mirror → `db_scripts`; add upgrade hop when also a version move.
