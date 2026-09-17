# db_upgrade_scripts/

Existing DBs. Pair `sql/{from}_to_{to}_upgrade.sql` + `_rollback.sql`. `upgrade.sh` runs `sql/${CURRENT_VERSION}_to_${UPGRADE_VERSION}_${ACTION}.sql` (`upgrade|rollback`).

Set `upgrade.properties` → `./upgrade.sh upgrade.properties`. New hop only — never edit released hops. Always ship rollback. Mirror → `db_scripts`.
