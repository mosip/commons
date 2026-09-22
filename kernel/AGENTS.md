# kernel/

`kernel-core`: published lib — keep impls + `io.mosip.kernel.core.*` + `PridValidator`. No `PridGenerator` / split artifacts / strip Spring (`BaseRepository`,`AuthUserDetails`).

OpenID/auth (from mosip-openid-bridge): reactor order `kernel-core` → `kernel-auth-adapter` (incl. openid bridge API packages) → `kernel-auth-service`, then others (`authcodeflowproxy`, notifier, idgen, config). Sibling deps omit `<version>`. Do not re-split openid-bridge-api or auth-adapter out while commons services depend on them.

idgen `:8080`: sole `IDGeneratorVertxApplication`; Vert.x `/v1/ridgenerator`+`/v1/idgenerator`; RID on Vert.x `HibernateDaoConfig`; Vert.x **3.9.16**; StaticHandler cache off; no 2nd Boot app.
notifier `:8083`: `/v1/notifier`; auth-adapter (reactor) + msg91 Maven deps (not wget).
config `:51000`: `/config`; `local,native`; after `conf-secrets`; no merge.

Pin Boot **4.1.1** · Cloud **2025.1.3** · Springdoc **3.1.1** on parent pom. Children inherit; HTTP mods take `kernel-core` unversioned. Boot4: ZIP `layout` not `<executable>true`. Preserve paths/tables/ID filters unless product asks.

`mvn -pl <mod> -am test` from `kernel/`. PS: `"-Dgpg.skip=true"`. JaCoCo `@{argLine}`. No `--` in XML comments. No invented `@author`. Local: `run-local.*` + service README.
