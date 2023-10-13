---
sidebar_position: 4
---

# Create a Simple Transaction

## Transfering LVLs

To create a simple transaction you need to run the following command:

```bash
brambl-cli simple-transaction create --from-party $PARTY --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token $TOKEN_TYPE
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the party `$PARTY` and sends `$SEND_AMOUNT` polys to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`.

The `--from-state` parameter is only required if the party is `noparty`. If the party is `self`, or any contract where there is at least one party, then the `--from-state` parameter is not required.


Alternatively, instead of providing an output address, the party and contract of the output can be used instead. To do this, run the following command:

```bash
brambl-cli simple-transaction create --from-party $FROM_PARTY --from-contract $FROM_CONTRACT --from-state $STATE --to-party $TO_PARTY --to-contract $TO_CONTRACT -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET
```

This will create a transaction that spends the state `$STATE` of the contract `$FROM_CONTRACT` of the party `$FROM_PARTY` and sends `$SEND_AMOUNT` polys to the contract `$TO_CONTRACT` of the party `$TO_PARTY`. The transaction will be stored in the file `$TX_FILE`. If no state is provided, the next state will be used
as defined in the cartesian indexing.

## Transfering Group Tokens

To transfer group tokens, you need to run the following command:

```bash
brambl-cli simple-transaction create --from-party $PARTY --from-contract $CONTRACT --from-state $STATE -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token group --group-id $GROUP_ID
```

This will create a transaction that spends the state `$STATE` of the contract `$CONTRACT` of the party `$PARTY` and sends `$SEND_AMOUNT` group tokens to the address `$TO_ADDRESS`. The transaction will be stored in the file `$TX_FILE`. The `--group-id` parameter is required to specify the group token to transfer.	