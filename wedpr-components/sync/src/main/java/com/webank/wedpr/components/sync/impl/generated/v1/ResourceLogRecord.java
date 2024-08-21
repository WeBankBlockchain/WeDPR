package com.webank.wedpr.components.sync.impl.generated.v1;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.contract.FunctionWrapper;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.TransactionManager;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ResourceLogRecord extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5060405161040438038061040483398101604081905261002f916100f8565b8051610042906001906020840190610049565b5050610202565b828054610055906101c7565b90600052602060002090601f01602090048101928261007757600085556100bd565b82601f1061009057805160ff19168380011785556100bd565b828001600101855582156100bd579182015b828111156100bd5782518255916020019190600101906100a2565b506100c99291506100cd565b5090565b5b808211156100c957600081556001016100ce565b634e487b7160e01b600052604160045260246000fd5b6000602080838503121561010b57600080fd5b82516001600160401b038082111561012257600080fd5b818501915085601f83011261013657600080fd5b815181811115610148576101486100e2565b604051601f8201601f19908116603f01168101908382118183101715610170576101706100e2565b81604052828152888684870101111561018857600080fd5b600093505b828410156101aa578484018601518185018701529285019261018d565b828411156101bb5760008684830101525b98975050505050505050565b600181811c908216806101db57607f821691505b602082108114156101fc57634e487b7160e01b600052602260045260246000fd5b50919050565b6101f3806102116000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806349081e201461003b578063f18ad6fb14610050575b600080fd5b61004e61004936600461010c565b600055565b005b61005861006f565b604051610066929190610125565b60405180910390f35b60006060600054600180805461008490610182565b80601f01602080910402602001604051908101604052809291908181526020018280546100b090610182565b80156100fd5780601f106100d2576101008083540402835291602001916100fd565b820191906000526020600020905b8154815290600101906020018083116100e057829003601f168201915b50505050509050915091509091565b60006020828403121561011e57600080fd5b5035919050565b82815260006020604081840152835180604085015260005b818110156101595785810183015185820160600152820161013d565b8181111561016b576000606083870101525b50601f01601f191692909201606001949350505050565b600181811c9082168061019657607f821691505b602082108114156101b757634e487b7160e01b600052602260045260246000fd5b5091905056fea264697066735822122068c69ed02143d218ffc0306a031f5fce34833e77232993d2bacf090801db9f7164736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5060405161040438038061040483398101604081905261002f916100f8565b8051610042906001906020840190610049565b5050610202565b828054610055906101c7565b90600052602060002090601f01602090048101928261007757600085556100bd565b82601f1061009057805160ff19168380011785556100bd565b828001600101855582156100bd579182015b828111156100bd5782518255916020019190600101906100a2565b506100c99291506100cd565b5090565b5b808211156100c957600081556001016100ce565b63b95aa35560e01b600052604160045260246000fd5b6000602080838503121561010b57600080fd5b82516001600160401b038082111561012257600080fd5b818501915085601f83011261013657600080fd5b815181811115610148576101486100e2565b604051601f8201601f19908116603f01168101908382118183101715610170576101706100e2565b81604052828152888684870101111561018857600080fd5b600093505b828410156101aa578484018601518185018701529285019261018d565b828411156101bb5760008684830101525b98975050505050505050565b600181811c908216806101db57607f821691505b602082108114156101fc5763b95aa35560e01b600052602260045260246000fd5b50919050565b6101f3806102116000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806307a0cc521461003b578063a3ec0aed1461005a575b600080fd5b61004361006f565b60405161005192919061010c565b60405180910390f35b61006d610068366004610169565b600055565b005b60006060600054600180805461008490610182565b80601f01602080910402602001604051908101604052809291908181526020018280546100b090610182565b80156100fd5780601f106100d2576101008083540402835291602001916100fd565b820191906000526020600020905b8154815290600101906020018083116100e057829003601f168201915b50505050509050915091509091565b82815260006020604081840152835180604085015260005b8181101561014057858101830151858201606001528201610124565b81811115610152576000606083870101525b50601f01601f191692909201606001949350505050565b60006020828403121561017b57600080fd5b5035919050565b600181811c9082168061019657607f821691505b602082108114156101b75763b95aa35560e01b600052602260045260246000fd5b5091905056fea264697066735822122081114ca6621b2baf61f2d049ee76c23deff8376ca8732db531723e2065b9e38164736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"_resourceRecord\",\"type\":\"string\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]},{\"kind\":4,\"value\":[1]}],\"inputs\":[],\"name\":\"getRecord\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[4052408059,127978578],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[{\"internalType\":\"int256\",\"name\":\"_index\",\"type\":\"int256\"}],\"name\":\"setIndex\",\"outputs\":[],\"selector\":[1225268768,2750155501],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GETRECORD = "getRecord";

    public static final String FUNC_SETINDEX = "setIndex";

    protected ResourceLogRecord(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
        this.transactionManager = new ProxySignTransactionManager(client);
    }

    protected ResourceLogRecord(
            String contractAddress, Client client, TransactionManager transactionManager) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, transactionManager);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public Tuple2<BigInteger, String> getRecord() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETRECORD,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Int256>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<BigInteger, String>(
                (BigInteger) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public Function getMethodGetRecordRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETRECORD,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Int256>() {},
                                new TypeReference<Utf8String>() {}));
        return function;
    }

    public TransactionReceipt setIndex(BigInteger _index) {
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(_index)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodSetIndexRawFunction(BigInteger _index) throws ContractException {
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(_index)),
                        Arrays.<TypeReference<?>>asList());
        return function;
    }

    public FunctionWrapper buildMethodSetIndex(BigInteger _index) {
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(_index)),
                        Arrays.<TypeReference<?>>asList());
        return new FunctionWrapper(this, function);
    }

    public String getSignedTransactionForSetIndex(BigInteger _index) {
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(_index)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String setIndex(BigInteger _index, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(_index)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<BigInteger> getSetIndexInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_SETINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static ResourceLogRecord load(
            String contractAddress, Client client, TransactionManager transactionManager) {
        return new ResourceLogRecord(contractAddress, client, transactionManager);
    }

    public static ResourceLogRecord load(String contractAddress, Client client) {
        return new ResourceLogRecord(
                contractAddress, client, new ProxySignTransactionManager(client));
    }

    public static ResourceLogRecord deploy(
            Client client, CryptoKeyPair credential, String _resourceRecord)
            throws ContractException {
        byte[] encodedConstructor =
                FunctionEncoder.encodeConstructor(
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(
                                        _resourceRecord)));
        ResourceLogRecord contract =
                deploy(
                        ResourceLogRecord.class,
                        client,
                        credential,
                        getBinary(client.getCryptoSuite()),
                        getABI(),
                        encodedConstructor,
                        null);
        contract.setTransactionManager(new ProxySignTransactionManager(client));
        return contract;
    }
}
