# db_release_scripts/

`mosip_kernel/sql/{version}-release.sql` + `{version}-revoke.sql`. Always ship revoke.

`cd db_release_scripts/mosip_kernel && ./deploy.sh deploy.properties <version>` (`revoke.sh` to undo). Mirror into `db_scripts`; add an upgrade hop when it is also a version move.
