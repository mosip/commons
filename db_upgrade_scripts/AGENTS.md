# db_upgrade_scripts/

Existing DBs. Pair `sql/{from}_to_{to}_upgrade.sql` + `_rollback.sql`. `upgrade.sh` runs `sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_${ACTION}.sql` (`upgrade|rollback`).

Set `upgrade.properties` then `./upgrade.sh upgrade.properties`. New hop only — do not edit released hops. Always ship rollback. Mirror into `db_scripts`.
