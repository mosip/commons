# kernel/

Order: `core` → `auth-adapter` → `auth-service` → authcodeflowproxy · notifier · idgen · config. Sibling deps omit `<version>`.

core: keep impls + `io.mosip.kernel.core.*` + `PridValidator`. No `PridGenerator` / split arts / strip Spring.
idgen `:8080` Vert.x 3.9.16 — sole `IDGeneratorVertxApplication`; `/v1/idgenerator`+`/v1/ridgenerator`; no 2nd Boot; **no springdoc UI**.
auth `:8091` `/v1/authmanager`. notifier `:8083` `/v1/notifier` (adapter+msg91 Maven, not wget). config `:51000` `/config` `local,native` after conf-secrets.

Pins (parent): Boot **4.1.1** · Cloud **2025.1.3** · Springdoc **3.1.1**. Boot4: ZIP `layout`. `mvn -pl <m> -am test`. PS `"-Dgpg.skip=true"`. JaCoCo `@{argLine}`. Local: `run-local.*` + README.
