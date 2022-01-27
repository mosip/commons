# Databases

## Overview
This folder containers various SQL scripts to create database and tables in postgres. The tables are described under `<db name>/ddl/`. Default data that's populated in the tables is present under `<db name>/dml` folder 

These scripts are automatically run when Kubernetes based sandbox is installed.  However, developers may run these scripts as given below.

This folder containers various SQL scripts to create database and tables in postgres.  These scripts are automatically run with [DB init](https://github.com/mosip/mosip-infra/blob/v1.2.0/deployment/v3/external/postgres/cluster/init_db.sh) of sandbox deployment.

Default data that's populated in the tables is present under `<db name>/dml` folder 


