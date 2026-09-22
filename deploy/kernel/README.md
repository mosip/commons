# Kernel 

## Overview
Refer [Commons](https://docs.mosip.io/1.2.0/modules/commons).

Installs **authmanager**, **idgenerator**, and **notifier** into the `kernel` namespace (charts under [`helm/`](../../helm)).

Authmanager chart was moved here from [mosip-openid-bridge](https://github.com/mosip/mosip-openid-bridge) (`helm/authmanager`).

## Install 
```
./install.sh
```
* During `install.sh`, a prompt asks whether you have a public domain and valid SSL certificate.
* Choose `n` only for development servers with self-signed certs (enables the insecure init-container path used by authmanager).
