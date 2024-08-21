package com.webank.wedpr.components.sync.impl.generated.v1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.v3.codec.datatypes.*;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.contract.FunctionWrapper;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.eventsub.EventSubCallback;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.ProxySignTransactionManager;
import org.fisco.bcos.sdk.v3.transaction.manager.transactionv1.TransactionManager;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ResourceLogRecordFactory extends Contract {
    public static final String[] BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5060405161085a38038061085a83398101604081905261002f91610054565b600080546001600160a01b0319166001600160a01b0392909216919091179055610084565b60006020828403121561006657600080fd5b81516001600160a01b038116811461007d57600080fd5b9392505050565b6107c7806100936000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c8063a971c7391461003b578063e51e431014610055575b600080fd5b61004361006a565b60405190815260200160405180910390f35b61006861006336600461026a565b6100e7565b005b60008060009054906101000a90046001600160a01b03166001600160a01b031663a971c7396040518163ffffffff1660e01b8152600401602060405180830381865afa1580156100be573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906100e2919061031f565b905090565b6000826040516100f690610247565b6101009190610338565b604051809103906000f08015801561011c573d6000803e3d6000fd5b50905060008060009054906101000a90046001600160a01b03166001600160a01b03166356d68baa6040518163ffffffff1660e01b81526004016020604051808303816000875af1158015610175573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610199919061031f565b60405163024840f160e51b8152600481018290529091506001600160a01b038316906349081e2090602401600060405180830381600087803b1580156101de57600080fd5b505af11580156101f2573d6000803e3d6000fd5b5050604080516001600160a01b0386168152602081018590529081018690527f39504972904512fd143ae589aa2303eed1357986795ed5ccc1b31a46e4dda65f9250606001905060405180910390a150505050565b6104048061038e83390190565b634e487b7160e01b600052604160045260246000fd5b6000806040838503121561027d57600080fd5b823567ffffffffffffffff8082111561029557600080fd5b818501915085601f8301126102a957600080fd5b8135818111156102bb576102bb610254565b604051601f8201601f19908116603f011681019083821181831017156102e3576102e3610254565b816040528281528860208487010111156102fc57600080fd5b826020860160208301376000602093820184015298969091013596505050505050565b60006020828403121561033157600080fd5b5051919050565b600060208083528351808285015260005b8181101561036557858101830151858201604001528201610349565b81811115610377576000604083870101525b50601f01601f191692909201604001939250505056fe608060405234801561001057600080fd5b5060405161040438038061040483398101604081905261002f916100f8565b8051610042906001906020840190610049565b5050610202565b828054610055906101c7565b90600052602060002090601f01602090048101928261007757600085556100bd565b82601f1061009057805160ff19168380011785556100bd565b828001600101855582156100bd579182015b828111156100bd5782518255916020019190600101906100a2565b506100c99291506100cd565b5090565b5b808211156100c957600081556001016100ce565b634e487b7160e01b600052604160045260246000fd5b6000602080838503121561010b57600080fd5b82516001600160401b038082111561012257600080fd5b818501915085601f83011261013657600080fd5b815181811115610148576101486100e2565b604051601f8201601f19908116603f01168101908382118183101715610170576101706100e2565b81604052828152888684870101111561018857600080fd5b600093505b828410156101aa578484018601518185018701529285019261018d565b828411156101bb5760008684830101525b98975050505050505050565b600181811c908216806101db57607f821691505b602082108114156101fc57634e487b7160e01b600052602260045260246000fd5b50919050565b6101f3806102116000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806349081e201461003b578063f18ad6fb14610050575b600080fd5b61004e61004936600461010c565b600055565b005b61005861006f565b604051610066929190610125565b60405180910390f35b60006060600054600180805461008490610182565b80601f01602080910402602001604051908101604052809291908181526020018280546100b090610182565b80156100fd5780601f106100d2576101008083540402835291602001916100fd565b820191906000526020600020905b8154815290600101906020018083116100e057829003601f168201915b50505050509050915091509091565b60006020828403121561011e57600080fd5b5035919050565b82815260006020604081840152835180604085015260005b818110156101595785810183015185820160600152820161013d565b8181111561016b576000606083870101525b50601f01601f191692909201606001949350505050565b600181811c9082168061019657607f821691505b602082108114156101b757634e487b7160e01b600052602260045260246000fd5b5091905056fea264697066735822122068c69ed02143d218ffc0306a031f5fce34833e77232993d2bacf090801db9f7164736f6c634300080b0033a264697066735822122039c5db7d7ee9caeed9c7ae3f602621e3391db2883ca14cef8a7c0172817e731064736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "608060405234801561001057600080fd5b5060405161085a38038061085a83398101604081905261002f91610054565b600080546001600160a01b0319166001600160a01b0392909216919091179055610084565b60006020828403121561006657600080fd5b81516001600160a01b038116811461007d57600080fd5b9392505050565b6107c7806100936000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80632b46e36f1461003b578063d0718b6a14610050575b600080fd5b61004e61004936600461026a565b61006a565b005b6100586101ca565b60405190815260200160405180910390f35b60008260405161007990610247565b610083919061031f565b604051809103906000f08015801561009f573d6000803e3d6000fd5b50905060008060009054906101000a90046001600160a01b03166001600160a01b031663f63437ac6040518163ffffffff1660e01b81526004016020604051808303816000875af11580156100f8573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061011c9190610374565b60405163a3ec0aed60e01b8152600481018290529091506001600160a01b0383169063a3ec0aed90602401600060405180830381600087803b15801561016157600080fd5b505af1158015610175573d6000803e3d6000fd5b5050604080516001600160a01b0386168152602081018590529081018690527fb325b2a35269f88854099942db2bbce0d8da3990d59a629fc1c9a4a4bd21408e9250606001905060405180910390a150505050565b60008060009054906101000a90046001600160a01b03166001600160a01b031663d0718b6a6040518163ffffffff1660e01b8152600401602060405180830381865afa15801561021e573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102429190610374565b905090565b6104048061038e83390190565b63b95aa35560e01b600052604160045260246000fd5b6000806040838503121561027d57600080fd5b823567ffffffffffffffff8082111561029557600080fd5b818501915085601f8301126102a957600080fd5b8135818111156102bb576102bb610254565b604051601f8201601f19908116603f011681019083821181831017156102e3576102e3610254565b816040528281528860208487010111156102fc57600080fd5b826020860160208301376000602093820184015298969091013596505050505050565b600060208083528351808285015260005b8181101561034c57858101830151858201604001528201610330565b8181111561035e576000604083870101525b50601f01601f1916929092016040019392505050565b60006020828403121561038657600080fd5b505191905056fe608060405234801561001057600080fd5b5060405161040438038061040483398101604081905261002f916100f8565b8051610042906001906020840190610049565b5050610202565b828054610055906101c7565b90600052602060002090601f01602090048101928261007757600085556100bd565b82601f1061009057805160ff19168380011785556100bd565b828001600101855582156100bd579182015b828111156100bd5782518255916020019190600101906100a2565b506100c99291506100cd565b5090565b5b808211156100c957600081556001016100ce565b63b95aa35560e01b600052604160045260246000fd5b6000602080838503121561010b57600080fd5b82516001600160401b038082111561012257600080fd5b818501915085601f83011261013657600080fd5b815181811115610148576101486100e2565b604051601f8201601f19908116603f01168101908382118183101715610170576101706100e2565b81604052828152888684870101111561018857600080fd5b600093505b828410156101aa578484018601518185018701529285019261018d565b828411156101bb5760008684830101525b98975050505050505050565b600181811c908216806101db57607f821691505b602082108114156101fc5763b95aa35560e01b600052602260045260246000fd5b50919050565b6101f3806102116000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806307a0cc521461003b578063a3ec0aed1461005a575b600080fd5b61004361006f565b60405161005192919061010c565b60405180910390f35b61006d610068366004610169565b600055565b005b60006060600054600180805461008490610182565b80601f01602080910402602001604051908101604052809291908181526020018280546100b090610182565b80156100fd5780601f106100d2576101008083540402835291602001916100fd565b820191906000526020600020905b8154815290600101906020018083116100e057829003601f168201915b50505050509050915091509091565b82815260006020604081840152835180604085015260005b8181101561014057858101830151858201606001528201610124565b81811115610152576000606083870101525b50601f01601f191692909201606001949350505050565b60006020828403121561017b57600080fd5b5035919050565b600181811c9082168061019657607f821691505b602082108114156101b75763b95aa35560e01b600052602260045260246000fd5b5091905056fea264697066735822122081114ca6621b2baf61f2d049ee76c23deff8376ca8732db531723e2065b9e38164736f6c634300080b0033a2646970667358221220752dd6ed17cf289a832876d1786bdbc4b576b586e7a21fbeebc9c46a8c4809c264736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[{\"internalType\":\"address\",\"name\":\"sequencerAddress\",\"type\":\"address\"}],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"address\",\"name\":\"recordAddress\",\"type\":\"address\"},{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"recordIndex\",\"type\":\"int256\"},{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"contractVersion\",\"type\":\"int256\"}],\"name\":\"addRecordEvent\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"recordContent\",\"type\":\"string\"},{\"internalType\":\"int256\",\"name\":\"contractVersion\",\"type\":\"int256\"}],\"name\":\"addRecord\",\"outputs\":[],\"selector\":[3843965712,726066031],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"getLatestIndex\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[2842806073,3497102186],\"stateMutability\":\"view\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ADDRECORD = "addRecord";

    public static final String FUNC_GETLATESTINDEX = "getLatestIndex";

    public static final Event ADDRECORDEVENT_EVENT =
            new Event(
                    "addRecordEvent",
                    Arrays.<TypeReference<?>>asList(
                            new TypeReference<Address>() {},
                            new TypeReference<Int256>() {},
                            new TypeReference<Int256>() {}));;

    protected ResourceLogRecordFactory(
            String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
        this.transactionManager = new ProxySignTransactionManager(client);
    }

    protected ResourceLogRecordFactory(
            String contractAddress, Client client, TransactionManager transactionManager) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, transactionManager);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<AddRecordEventEventResponse> getAddRecordEventEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList =
                extractEventParametersWithLog(ADDRECORDEVENT_EVENT, transactionReceipt);
        ArrayList<AddRecordEventEventResponse> responses =
                new ArrayList<AddRecordEventEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AddRecordEventEventResponse typedResponse = new AddRecordEventEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.recordAddress =
                    (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.recordIndex =
                    (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.contractVersion =
                    (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeAddRecordEventEvent(
            BigInteger fromBlock,
            BigInteger toBlock,
            List<String> otherTopics,
            EventSubCallback callback) {
        String topic0 = eventEncoder.encode(ADDRECORDEVENT_EVENT);
        subscribeEvent(topic0, otherTopics, fromBlock, toBlock, callback);
    }

    public void subscribeAddRecordEventEvent(EventSubCallback callback) {
        String topic0 = eventEncoder.encode(ADDRECORDEVENT_EVENT);
        subscribeEvent(topic0, callback);
    }

    public TransactionReceipt addRecord(String recordContent, BigInteger contractVersion) {
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(recordContent),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(
                                        contractVersion)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodAddRecordRawFunction(String recordContent, BigInteger contractVersion)
            throws ContractException {
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(recordContent),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(
                                        contractVersion)),
                        Arrays.<TypeReference<?>>asList());
        return function;
    }

    public FunctionWrapper buildMethodAddRecord(String recordContent, BigInteger contractVersion) {
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(recordContent),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(
                                        contractVersion)),
                        Arrays.<TypeReference<?>>asList());
        return new FunctionWrapper(this, function);
    }

    public String getSignedTransactionForAddRecord(
            String recordContent, BigInteger contractVersion) {
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(recordContent),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(
                                        contractVersion)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String addRecord(
            String recordContent, BigInteger contractVersion, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String(recordContent),
                                new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256(
                                        contractVersion)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple2<String, BigInteger> getAddRecordInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_ADDRECORD,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(
                (String) results.get(0).getValue(), (BigInteger) results.get(1).getValue());
    }

    public BigInteger getLatestIndex() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETLATESTINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public Function getMethodGetLatestIndexRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_GETLATESTINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return function;
    }

    public static ResourceLogRecordFactory load(
            String contractAddress, Client client, TransactionManager transactionManager) {
        return new ResourceLogRecordFactory(contractAddress, client, transactionManager);
    }

    public static ResourceLogRecordFactory load(String contractAddress, Client client) {
        return new ResourceLogRecordFactory(
                contractAddress, client, new ProxySignTransactionManager(client));
    }

    public static ResourceLogRecordFactory deploy(
            Client client, CryptoKeyPair credential, String sequencerAddress)
            throws ContractException {
        byte[] encodedConstructor =
                FunctionEncoder.encodeConstructor(
                        Arrays.<Type>asList(
                                new org.fisco.bcos.sdk.v3.codec.datatypes.Address(
                                        sequencerAddress)));
        ResourceLogRecordFactory contract =
                deploy(
                        ResourceLogRecordFactory.class,
                        client,
                        credential,
                        getBinary(client.getCryptoSuite()),
                        getABI(),
                        encodedConstructor,
                        null);
        contract.setTransactionManager(new ProxySignTransactionManager(client));
        return contract;
    }

    public static class AddRecordEventEventResponse {
        public TransactionReceipt.Logs log;

        public String recordAddress;

        public BigInteger recordIndex;

        public BigInteger contractVersion;
    }
}
