# kernel/

`kernel-core`: published lib — keep impls (generators, validators, Hibernate, PDF, websub, templates) and `io.mosip.kernel.core.*`. Keep `PridValidator`. No `PridGenerator`, no split to `kernel-*` artifacts, no strip Spring (`BaseRepository`, `AuthUserDetails`).

idgenerator `:8080`: sole main `IDGeneratorVertxApplication`; Vert.x `/v1/ridgenerator` + `/v1/idgenerator`; RID beans on Vert.x `HibernateDaoConfig` (`RidFetcherRouter`). Vert.x **3.9.16** (`UinServiceRouter` already disables StaticHandler cache). No second Boot app.

notifier `:8083`: separate JVM; `/v1/notifier`; `kernel-auth-adapter` + `kernel-smsserviceprovider-msg91` are Maven deps (not Docker wget).

config-server `:51000`: `/config`; local profile `local,native`; after `conf-secrets`. Do not merge services.

Pin Boot **4.1.1** · Cloud **2025.1.3** · Springdoc **3.1.1** on `pom.xml` (`properties` + `dependencyManagement`). Children inherit plugins; HTTP modules take `kernel-core` with no `<version>`. Boot 4: `layout` ZIP, not `<executable>true`. Preserve servlet paths, `mosip_kernel` tables, ID filters unless product asks.

`mvn -pl <mod> -am test` from `kernel/`. PowerShell: `"-Dgpg.skip=true"`. JaCoCo: `@{argLine}`. XML comments: no `--`. No invented `@author`. Local: `run-local.sh` / `run-local.bat` + each service `README.md`.
