### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: Release DB deployment
### -- Deploy Module 	: MOSIP Commons
### -- Purpose    		: To deploy MOSIP Database alter scripts for the release, Scripts deploy changes for commons databases.       
### -- Create By   		: Sadanandegowda
### -- Created Date		: 07-Jan-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects deployment for the release started.... Release Number : $1"

echo "=============================================================================================================="
bash ./mosip_kernel/kernel_release_db_deploy.sh ./mosip_kernel/kernel_release_deploy.properties $1
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_idrepo/idrepo_release_db_deploy.sh ./mosip_idrepo/idrepo_release_deploy.properties $1
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_idmap/idmap_release_db_deploy.sh ./mosip_idmap/idmap_release_deploy.properties $1
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_audit/audit_release_db_deploy.sh ./mosip_audit/audit_release_deploy.properties $1
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_iam/iam_release_db_deploy.sh ./mosip_iam/iam_release_deploy.properties $1
echo "=============================================================================================================="


echo "`date` : MOSIP DB Release Deployment for core databases is completed, Please check the logs at respective logs directory for more information"
 
