#!/bin/bash
###########################################################
# create by feinenxiang
###########################################################

EXIT_CODE=-1

check_env() {
    version=`openssl version 2>&1 | grep 1.1.1`
    [ -z "$version" ] && {
        echo "please install openssl 1.1.1"
        #echo "please install openssl 1.1.1!"
        #echo "download openssl from https://www.openssl.org."
        echo "use \"openssl version\" command to check."
        exit $EXIT_CODE
    }
}
check_env

check_java() {
    ver=`java -version 2>&1 | grep version | grep 1.8`
    tm=`java -version 2>&1 | grep "Java(TM)"`
    [ -z "$ver" -o -z "$tm" ] && {
        echo "please install java Java(TM) 1.8 series!"
        echo "use \"java -version\" command to check."
        exit $EXIT_CODE
    }

    which keytool >/dev/null 2>&1
    [ $? != 0 ] && {
        echo "keytool command not exists!"
        exit $EXIT_CODE
    }
}
# 1.make root ca
# bash cert_script.sh gen_chain_cert ca

# 2.make agency csr
# bash cert_script.sh gen_rsa_req agency webank

# 3.sign agency cert
# bash cert_script.sh sign_agency_cert ca webank/agency.csr 365

usage() {
printf "%s\n" \
"usage command gen_rsa_req type reqdir|
              gen_secp_req type reqdir|
              gen_chain_cert chaindir|
              gen_agency_cert chaindir agencydir|
              sign_agency_cert chaindir reqfile days|
              gen_node_cert agencydir nodedir|
              sign_node_cert agencydir reqfile|
              gen_sdk_cert agencydir sdkdir|
              sign_sdk_cert agencydir reqfile|
              merge_sdk_cert mergedir|
              help"
}

getname() {
    local name="$1"
    if [ -z "$name" ]; then
        return 0
    fi
    [[ "$name" =~ ^.*/$ ]] && {
        name="${name%/*}"
    }
    name="${name##*/}"
    echo "$name"
}

check_name() {
    local name="$1"
    local value="$2"
    [[ "$value" =~ ^[a-zA-Z0-9._-]+$ ]] || {
        echo "$name name [$value] invalid, it should match regex: ^[a-zA-Z0-9._-]+\$"
        exit $EXIT_CODE
    }
}

file_must_exists() {
    if [ ! -f "$1" ]; then
        echo "$1 file does not exist, please check!"
        exit $EXIT_CODE
    fi
}

file_must_not_exists() {
    if [ -f "$1" ]; then
        echo "$1 file exists, please delete old file!"
        exit $EXIT_CODE
    fi
}

dir_must_exists() {
    if [ ! -d "$1" ]; then
        echo "$1 DIR does not exist, please check!"
        exit $EXIT_CODE
    fi
}

dir_must_not_exists() {
    if [ -e "$1" ]; then
        echo "$1 DIR exists, please clean old DIR!"
        exit $EXIT_CODE
    fi
}

get_req_common_name() {
    __req="$1"
    __commonname=`openssl req -noout -subject -in $__req | awk -F/ '{for(i=1;i<=NF;i++){if($i~/^CN=/)print $i}}' | awk -F= '{print $2}'`
    echo $__commonname
}

get_req_pubkey() {
    __req="$1"
    __pubkey=`openssl req -pubkey -outform DER -noout -in $__req | awk '!/^---/{printf $0}' | base64 -d | xxd -g0 -p | awk '{printf $0}'`
    echo $__pubkey
}

gen_chain_cert() {
    path="$1"
    name=`getname "$path"`
    dir_must_not_exists "$path"
    check_name chain "$name"

    chaindir=$path
    mkdir -p $chaindir
    openssl genrsa -out $chaindir/ca.key 2048
    openssl req -new -x509 -days 3650 -subj "/CN=$name/O=fiscobcos/OU=chain" -key $chaindir/ca.key -out $chaindir/ca.crt

    if [ $? -eq 0 ]; then
        echo "build chain ca succussful!"
    else
        echo "please input at least Common Name!"
    fi
}

gen_rsa_req() {
    type="$1"
    path="$2"
    name=`getname "$path"`
    if [ "${type}" != "agency" ]; then
        echo "type must be \"agency\"!"
        exit $EXIT_CODE
    fi
    check_name ${type} "$name"
    indir=$path
    dir_must_not_exists "$indir"
    mkdir -p $indir
    openssl genrsa -out $indir/${type}.key 2048
    openssl req -new -sha256 -subj "/CN=$name/O=fiscobcos/OU=${type}" -key $indir/${type}.key -config cert.cnf -out $indir/${type}.csr
}

gen_agency_cert() {
    chain="$1"
    dir_must_exists "$chain"
    file_must_exists "$chain/ca.key"
    gen_rsa_req agency "$2"

    openssl x509 -req -days 3650 -sha256 -CA $chain/ca.crt -CAkey $chain/ca.key -CAcreateserial\
        -in $indir/agency.csr -out $indir/agency.crt  -extensions v4_req -extfile cert.cnf

    cp $chain/ca.crt $indir/
    cp $chain/ca.crt $indir/ca-agency.crt
    cat $indir/agency.crt >>$indir/ca-agency.crt
    rm -f $indir/agency.csr

    echo "build $name agency cert successful!"
}

sign_agency_cert() {
    chain="$1"
    req="$2"
    days="$3"
    dir_must_exists "$chain"
    file_must_exists "$chain/ca.key"
    file_must_exists "$req"
    indir=`dirname "$req"`

    commonname=`get_req_common_name $req`
    openssl x509 -req -days $days -sha256 -CA $chain/ca.crt -CAkey $chain/ca.key -CAcreateserial\
        -in $req -out $indir/agency.crt  -extensions v4_req -extfile cert.cnf

    cp $chain/ca.crt $indir/
    cp $chain/ca.crt $indir/ca-agency.crt
    cat $indir/agency.crt >>$indir/ca-agency.crt
    rm -f $req

    echo "build $commonname agency cert successful!"
}

check_secp_support() {
    if [ "" = "`openssl ecparam -list_curves 2>&1 | grep secp256k1`" ]; then
        echo "openssl don't support secp256k1, please upgrade openssl!"
        exit $EXIT_CODE
    fi
}

gen_secp_req() {
    type="$1"
    path="$2"
    name=`getname "$path"`
    if [ "${type}" != "node" -a "${type}" != "sdk" ]; then
        echo "type must be \"node\" or \"sdk\"!"
        exit $EXIT_CODE
    fi
    check_name ${type} "$name"
    indir=$path
    dir_must_not_exists "$indir"

    #check openssl support
    check_secp_support

    mkdir -p $indir
    openssl ecparam -out $indir/${type}.param -name secp256k1
    openssl genpkey -paramfile $indir/${type}.param -out $indir/${type}.key
    openssl pkey -in $indir/${type}.key -pubout -out $indir/${type}.pubkey
    openssl req -new -sha256 -subj "/CN=${name}/O=fiscobcos/OU=${type}" -key $indir/${type}.key -config cert.cnf -out $indir/${type}.csr
    if [ "${type}" = "node" ]; then
        #nodeid is pubkey
        openssl ec -in $indir/${type}.key -text -noout | sed -n '7,11p' | tr -d ": \n" | awk '{print substr($0,3);}' | cat >$indir/${type}.nodeid
    fi
}

gen_cert_secp256k1() {
    type="$1"
    capath="$2"
    req="$3"
    certpath=`dirname "$req"`
    openssl req -pubkey -noout -in $req -out $certpath/tmp.pubkey
    openssl x509 -req -days 3650 -sha256 -in $req -CAkey $capath/agency.key -CA $capath/agency.crt\
        -force_pubkey $certpath/tmp.pubkey -out $certpath/${type}.crt -CAcreateserial -extensions v3_req -extfile cert.cnf
    openssl ec -in $certpath/${type}.key -outform DER | tail -c +8 | head -c 32 | xxd -p -c 32 | cat >$certpath/${type}.private
    if [ "${type}" = "node" ]; then
        openssl x509 -serial -noout -in $certpath/${type}.crt | awk -F= '{print $2}' | cat >$certpath/${type}.serial
    fi
    cp $capath/ca-agency.crt $certpath/ca.crt
    cp $capath/agency.crt $certpath
    rm -rf $certpath/tmp.pubkey
}

get_nodeid() {
    req="$1"
    pubkey=`get_req_pubkey $req`
    lenofkey=`echo $pubkey | wc -c`
    ((startpos=lenofkey-128))
    nodeid=`expr substr $pubkey $startpos 128`
    echo $nodeid
}

gen_node_config() {
    agency=$1
    ndpath=$2
    req=$3
    nodeid=`get_nodeid $req`
    serial=`cat $ndpath/node.serial | head`

    cd $ndpath
    cat >node.json <<EOF
{
 "id":"$nodeid",
 "name":"$name",
 "agency":"$agency",
 "caHash":"$serial"
}
EOF
	cat >node.ca <<EOF
{
 "serial":"$serial",
 "pubkey":"$nodeid",
 "name":"$name"
}
EOF
}

gen_node_cert() {
    agpath="$1"
    ndpath="$2"
    agency=`getname "$agpath"`
    dir_must_exists "$agpath"
    file_must_exists "$agpath/agency.key"
    check_name agency "$agency"

    gen_secp_req node "$ndpath"
    gen_cert_secp256k1 node "$agpath" "$ndpath"/node.csr
    gen_node_config $agency $ndpath "$ndpath"/node.csr
    rm -f $certpath/${type}.csr

    echo "build $name node cert successful!"
}

sign_node_cert() {
    agpath="$1"
    req="$2"
    agency=`getname "$agpath"`
    dir_must_exists "$agpath"
    file_must_exists "$agpath/agency.key"
    check_name agency "$agency"
    ndpath=`dirname "$req"`

    gen_cert_secp256k1 node "$agpath" $req
    name=`get_req_common_name $req`
    gen_node_config $agency $ndpath $req
    rm -f $certpath/${type}.csr

    echo "build $name node cert successful!"
}

check_password() {
    if [ -n "$1" ]; then
        [[ "$1" =~ ^[a-zA-Z0-9._-]{6,}$ ]] || {
            echo "password invalid, at least 6 digits, should match regex: ^[a-zA-Z0-9._-]{6,}\$"
            exit $EXIT_CODE
        }
    fi
}

read_password() {
    read -se -p "Enter password for keystore:" pass1
    echo
    read -se -p "Verify password for keystore:" pass2
    echo
    [[ "$pass1" =~ ^[a-zA-Z0-9._-]{6,}$ ]] || {
        echo "password invalid, at least 6 digits, should match regex: ^[a-zA-Z0-9._-]{6,}\$"
        exit $EXIT_CODE
    }
    [ "$pass1" != "$pass2" ] && {
        echo "Verify password failure!"
        exit $EXIT_CODE
    }
    mypass=$pass1
}

gen_sdk_cert() {
    check_java

    agency="$1"
    sdkpath="$2"
    mypass="$3"
    dir_must_exists "$agency"
    file_must_exists "$agency/agency.key"
    check_password "$mypass"

    gen_secp_req sdk "$sdkpath"
    gen_cert_secp256k1 sdk "$agency" "$sdkpath"/sdk.csr
    rm -rf "$sdkpath"/sdk.csr

    [ -z "$mypass" ] && read_password
    openssl pkcs12 -export -name client -passout "pass:$mypass" -in $sdkpath/sdk.crt -inkey $sdkpath/sdk.key -out $sdkpath/keystore.p12
    keytool -importkeystore -srckeystore $sdkpath/keystore.p12 -srcstoretype pkcs12 -srcstorepass $mypass\
        -destkeystore $sdkpath/client.keystore -deststoretype jks -deststorepass $mypass -alias client 2>/dev/null

    echo "build $name sdk cert successful!"
}

sign_sdk_cert() {
    check_java

    agency="$1"
    req="$2"
    dir_must_exists "$agency"
    file_must_exists "$agency/agency.key"
    check_password "$mypass"
    sdkpath=`dirname "$req"`

    name=`get_req_common_name $req`
    gen_cert_secp256k1 sdk "$agency" $req
    rm -rf $req

    echo "build $name sdk cert successful!"
}

merge_sdk_cert() {
    sdkpath="$1"
    mypass="$2"
    check_password "$mypass"

    dir_must_exists "$sdkpath"
    file_must_exists "$sdkpath/sdk.crt"
    file_must_exists "$sdkpath/sdk.key"
    file_must_not_exists "$sdkpath/keystore.p12"
    file_must_not_exists "$sdkpath/client.keystore"

    [ -z "$mypass" ] && read_password
    openssl pkcs12 -export -name client -passout "pass:$mypass" -in $sdkpath/sdk.crt -inkey $sdkpath/sdk.key -out $sdkpath/keystore.p12
    keytool -importkeystore -srckeystore $sdkpath/keystore.p12 -srcstoretype pkcs12 -srcstorepass $mypass\
        -destkeystore $sdkpath/client.keystore -deststoretype jks -deststorepass $mypass -alias client 2>/dev/null
}

case "$1" in
gen_rsa_req)
    gen_rsa_req "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
gen_secp_req)
    gen_secp_req "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
gen_chain_cert)
    gen_chain_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
gen_agency_cert)
    gen_agency_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
sign_agency_cert)
    sign_agency_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
gen_node_cert)
    gen_node_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
sign_node_cert)
    sign_node_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
gen_sdk_cert)
    gen_sdk_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
sign_sdk_cert)
    sign_sdk_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
merge_sdk_cert)
    merge_sdk_cert "$2" "$3" "$4" "$5" "$6" "$7" "$8" "$9"
    ;;
help)
    usage
    ;;
*)
    usage
esac
