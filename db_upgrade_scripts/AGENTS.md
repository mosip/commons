# db_upgrade_scripts/

Existing DBs. Pair `sql/{from}_to_{to}_upgrade.sql` + `_rollback.sql`. `./upgrade.sh upgrade.properties` (`ACTION=upgrade|rollback`).

New hop only — never edit released. Always ship rollback. Mirror → `db_scripts`.
