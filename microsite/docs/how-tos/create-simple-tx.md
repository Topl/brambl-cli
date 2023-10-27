---
sidebar_position: 4
---

# Create a Simple Transaction

## Transfering LVLs

To create a simple transaction you need to run the following command:

```bash
brambl-cli simple-transaction create --from-fellowship $FELLOWSHIP --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token $TOKEN_TYPE
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the fellowship `$FELLOWSHIP` and sends `$SEND_AMOUNT` polys to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`.

The `--from-state` parameter is only required if the fellowship is `nofellowship`. If the fellowship is `self`, or any contract where there is at least one fellowship, then the `--from-state` parameter is not required.

We can pass the parameters `--change-fellowship`, `--change-contract`, and `--change-state`
if we want to send the change to a different fellowship, contract and state. If these parameters are not provided, the change will be sent to the same fellowship, contract and the next state
of the contract/fellowship pair. The transfers starting from the `nofellowship` fellowship require the change parameters to be provided.

Alternatively, instead of providing an output address, the fellowship and contract of the output can be used instead. To do this, run the following command:

```bash
brambl-cli simple-transaction create --from-fellowship $FROM_FELLOWSHIP --from-contract $FROM_CONTRACT --from-state $STATE --to-fellowship $TO_FELLOWSHIP --to-contract $TO_CONTRACT -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET
```

This will create a transaction that spends the state `$STATE` of the contract `$FROM_CONTRACT` of the fellowship `$FROM_FELLOWSHIP` and sends `$SEND_AMOUNT` polys to the contract `$TO_CONTRACT` of the fellowship `$TO_FELLOWSHIP`. The transaction will be stored in the file `$TX_FILE`. If no state is provided, the next state will be used
as defined in the cartesian indexing.

## Transfering Group Tokens

To transfer group tokens, you need to run the following command:

```bash
brambl-cli simple-transaction create --from-fellowship $FELLOWSHIP --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token group --group-id $GROUP_ID
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the fellowship `$FELLOWSHIP` and sends `$SEND_AMOUNT` group tokens to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`. The `--group-id` parameter is required to specify the group token to transfer.	

# Transfering Series Tokens

To transfer series tokens, you need to run the following command:

```bash
brambl-cli simple-transaction create --from-fellowship $FELLOWSHIP --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token series --series-id $SERIES_ID
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the fellowship `$FELLOWSHIP` and sends `$SEND_AMOUNT` series tokens to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`. The `--series-id` parameter is required to specify the series token to transfer.

# Transfering Asset Tokens

To transfer asset tokens, you need to run the following command:

```bash
brambl-cli simple-transaction create --from-fellowship $FELLOWSHIP --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token asset --group-id $GROUP_ID --series-id $SERIES_ID
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the fellowship `$FELLOWSHIP` and sends `$SEND_AMOUNT` asset tokens to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`. The `--group-id` and `--series-id` parameters are required to specify the group and series tokens to transfer. The asset is described by the pair `(group_id, series_id)`.