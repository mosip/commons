# db_release_scripts/

`mosip_kernel/sql/{ver}-release.sql` + `{ver}-revoke.sql`. Always ship revoke.

`cd db_release_scripts/mosip_kernel && ./deploy.sh deploy.properties <ver>` (`revoke.sh` undo). Mirror → `db_scripts`; add upgrade hop on version move.
