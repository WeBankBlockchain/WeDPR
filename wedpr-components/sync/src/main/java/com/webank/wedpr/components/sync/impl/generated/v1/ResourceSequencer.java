package com.webank.wedpr.components.sync.impl.generated.v1;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
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
public class ResourceSequencer extends Contract {
    public static final String[] BINARY_ARRAY = {
        "60806040526000805534801561001457600080fd5b5060fc806100236000396000f3fe6080604052348015600f57600080fd5b506004361060325760003560e01c806356d68baa146037578063a971c73914604f575b600080fd5b603d6056565b60405190815260200160405180910390f35b600054603d565b6000600160008082825460689190608b565b9091555050600054919050565b634e487b7160e01b600052601160045260246000fd5b600080821280156001600160ff1b038490038513161560aa5760aa6075565b600160ff1b839003841281161560c05760c06075565b5050019056fea2646970667358221220c15ef00b2352524240552dd9fecc23c21bdee53df26c19e1f465ec73dff3932364736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040526000805534801561001457600080fd5b5060f5806100236000396000f3fe6080604052348015600f57600080fd5b506004361060325760003560e01c8063d0718b6a146037578063f63437ac14604d575b600080fd5b6000545b60405190815260200160405180910390f35b603b60006001600080828254606191906084565b9091555050600054919050565b63b95aa35560e01b600052601160045260246000fd5b600080821280156001600160ff1b038490038513161560a35760a3606e565b600160ff1b839003841281161560b95760b9606e565b5050019056fea2646970667358221220abfbb5542239725d678784bc40b379d5d77d422764028222288b1f5e47da40d464736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"allocateIndex\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[1456901034,4130617260],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":4,\"value\":[0]}],\"inputs\":[],\"name\":\"getLatestIndex\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[2842806073,3497102186],\"stateMutability\":\"view\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_ALLOCATEINDEX = "allocateIndex";

    public static final String FUNC_GETLATESTINDEX = "getLatestIndex";

    protected ResourceSequencer(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
        this.transactionManager = new ProxySignTransactionManager(client);
    }

    protected ResourceSequencer(
            String contractAddress, Client client, TransactionManager transactionManager) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, transactionManager);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt allocateIndex() {
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public Function getMethodAllocateIndexRawFunction() throws ContractException {
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return function;
    }

    public FunctionWrapper buildMethodAllocateIndex() {
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        return new FunctionWrapper(this, function);
    }

    public String getSignedTransactionForAllocateIndex() {
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public String allocateIndex(TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public Tuple1<BigInteger> getAllocateIndexOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_ALLOCATEINDEX,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
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

    public static ResourceSequencer load(
            String contractAddress, Client client, TransactionManager transactionManager) {
        return new ResourceSequencer(contractAddress, client, transactionManager);
    }

    public static ResourceSequencer load(String contractAddress, Client client) {
        return new ResourceSequencer(
                contractAddress, client, new ProxySignTransactionManager(client));
    }

    public static ResourceSequencer deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        ResourceSequencer contract =
                deploy(
                        ResourceSequencer.class,
                        client,
                        credential,
                        getBinary(client.getCryptoSuite()),
                        getABI(),
                        null,
                        null);
        contract.setTransactionManager(new ProxySignTransactionManager(client));
        return contract;
    }
}
