---
sidebar_position: 4
---

# Bifrost Query Mode

```  
Command: bifrost-query [block-by-height|block-by-id|transaction-by-id] [options]
Bifrost query mode
Command: bifrost-query block-by-height
Get the block at a given height
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --height <value>         The height of the block. (mandatory)
Command: bifrost-query block-by-id
Get the block with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --block-id <value>       The id of the block in base 58. (mandatory)
Command: bifrost-query transaction-by-id
Get the transaction with a given id
  -h, --host <value>       The host of the node. (mandatory)
  --bifrost-port <value>   Port Bifrost node. (mandatory)
  --transaction-id <value>
                           The id of the transaction in base 58. (mandatory)
                           
```