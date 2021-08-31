#!/bin/sh
echo ""
echo ""
echo "---------------------start---------------------"
#配置
#项目前辍
PROJECT_PREFIX=qile
#svn项目地址
PROJECT_DIR=/d/qile-server/qile/qipai
#包输出目录
OUT_DIR=/d/serverpackage/server

#当前目录
CUR_DIR=`pwd`
#配置文件，配置需要保留的文件
PROP_FILE="/d/qile-server/qile/qipai/src/scripts/server.properties"

#打包的目标目录
PACKAGE_DIR="${OUT_DIR}/server"
if [ ! -d ${PACKAGE_DIR} ];then
    cd ${OUT_DIR}
    mkdir "server"
fi


echo PROJECT_DIR=${PROJECT_DIR}
echo OUT_DIR=${OUT_DIR}
echo PROP_FILE=${PROP_FILE}
echo PACKAGE_DIR="${PACKAGE_DIR}"



echo ""
echo ""
echo "---------------------git update---------------------"
cd ${PROJECT_DIR}

git pull

if [ $? -eq 0 ]; then
    echo "git update successful"
else
    echo "git update error"
    exit 1
fi


echo ""
echo ""
echo "---------------------read properties---------------------"
cd ${OUT_DIR}
declare -A map
for line in  `cat ${PROP_FILE} | grep -v "#" | awk '{if(length != 0) print $0}'`
do
    if [[ ! ${line} =~ ^\#.* ]]; then
        key=`echo ${line}`
        map[${key}]="1"
    fi
done

for key in ${!map[@]};do
    echo ${key}=${map[${key}]}
done


#SVN_VERSION=`svn info | grep Revision: | awk '{print $2}'`
SVN_VERSION=""
echo SVN_VERSION=${SVN_VERSION}


echo ""
echo ""
echo "---------------------mvn build---------------------"
cd ${PROJECT_DIR}
mvn clean install -U -Dmaven.test.skip=true

if [ $? -eq 0 ]; then
    echo "mvn build successful"
else
    echo "mvn build error"
    exit 1
fi


echo ""
echo ""
echo "---------------------copy project---------------------"

rm -rf "${PACKAGE_DIR}/WEB-INF"
TARGET_DIR="${PROJECT_DIR}/qipai-starter/target/game-starter-1.0-SNAPSHOT/WEB-INF"
# echo TARGET_DIR=${TARGET_DIR}
cp -r ${TARGET_DIR} ${PACKAGE_DIR}

#echo ${PACKAGE_DIR}
rm -rf "${PACKAGE_DIR}/WEB-INF/web.xml"
rm -rf "${PACKAGE_DIR}/WEB-INF/lib/lombok-1.18.2.jar"
rm -rf "${PACKAGE_DIR}/WEB-INF/lib/lombok-0.0.17.pom"
rm -rf "${PACKAGE_DIR}/WEB-INF/lib/game-lib-1.0-SNAPSHOT.jar"

#----------------------删除lib目录下不需要的文件-------------------
all_jar=`echo ${map["all_jar"]}`
if [[ ${all_jar} != "1" ]];then
    LIB_DIR="${PACKAGE_DIR}/WEB-INF/lib"
    fils=`ls ${LIB_DIR}`
    for file in ${fils}
    do 
        ret=`echo ${map[${file}]}`
        if [[ "1" != ${ret} ]]; then
            rm -rf "${LIB_DIR}/${file}"
        else
            echo "${LIB_DIR}/${file}"
        fi
    done
    ##目录为空则删除目录
    delDir=`ls -A ${LIB_DIR} | wc -w`
    if [ "0" -eq ${delDir} ];then
        rm -rf "${LIB_DIR}"
    fi
fi


#----------------------删除csv目录下不需要的文件-------------------
all_csv=`echo ${map["all_csv"]}`
if [[ ${all_csv} != "1" ]];then
    CSV_DIR="${PACKAGE_DIR}/WEB-INF/csv"
    fils=`ls ${CSV_DIR}`
    for file in ${fils}
    do 
        ret=`echo ${map[${file}]}`
        if [[ "1" != ${ret} ]]; then
            rm -rf "${CSV_DIR}/${file}"
        else
            echo "${CSV_DIR}/${file}"
        fi
    done
    ##目录为空则删除目录
    delDir=`ls -A ${CSV_DIR} | wc -w`
    if [ "0" -eq ${delDir} ];then
        rm -rf "${CSV_DIR}"
    fi
fi

#----------------------删除config目录下不需要的文件-------------------
CONFIG_DIR="${PACKAGE_DIR}/WEB-INF/config"

fils=`ls ${CONFIG_DIR}`
for file in ${fils}
do 
    ret=`echo ${map[${file}]}`
    if [[ "1" != ${ret} ]]; then
        rm -rf "${CONFIG_DIR}/${file}"
    else
        echo "${CONFIG_DIR}/${file}"
    fi
done
##目录为空则删除目录
delDir=`ls -A ${CONFIG_DIR} | wc -w`
if [ "0" -eq ${delDir} ];then
    rm -rf "${CONFIG_DIR}"
fi


echo ""
echo ""
echo "---------------------package project---------------------"

DATE_STR=`date -d today +%m%d%H%M%S`

cd ${PACKAGE_DIR}

PACKAGE_FILE_NAME="${PROJECT_PREFIX}_server_${DATE_STR}.tar.gz"
tar -zcf ${PACKAGE_FILE_NAME} WEB-INF

if [ $? -eq 0 ]; then
    echo packageName=${PACKAGE_FILE_NAME}
    echo "package project successful"
else
    echo "package project error"
    exit 1
fi
