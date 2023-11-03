"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[1802],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>d});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),c=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},p=function(e){var t=c(e.components);return r.createElement(l.Provider,{value:t},e.children)},u="mdxType",h={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),u=c(n),m=a,d=u["".concat(l,".").concat(m)]||u[m]||h[m]||o;return n?r.createElement(d,i(i({ref:t},p),{},{components:n})):r.createElement(d,i({ref:t},p))}));function d(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=m;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s[u]="string"==typeof e?e:a,i[1]=s;for(var c=2;c<o;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}m.displayName="MDXCreateElement"},3522:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>l,contentTitle:()=>i,default:()=>h,frontMatter:()=>o,metadata:()=>s,toc:()=>c});var r=n(7462),a=(n(7294),n(3905));const o={sidebar_position:3},i="Minting an Asset",s={unversionedId:"tutorials/mint-asset",id:"tutorials/mint-asset",title:"Minting an Asset",description:"In this tutorial we are going to mint an asset using the brambl-cli.",source:"@site/docs/tutorials/mint-asset.md",sourceDirName:"tutorials",slug:"/tutorials/mint-asset",permalink:"/brambl-cli/docs/current/tutorials/mint-asset",draft:!1,tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Creating Transactions",permalink:"/brambl-cli/docs/current/tutorials/create-tx"},next:{title:"How Tos",permalink:"/brambl-cli/docs/current/category/how-tos"}},l={},c=[{value:"Create the Group Constructor Token",id:"create-the-group-constructor-token",level:2},{value:"Create the Series Constructor Token",id:"create-the-series-constructor-token",level:2},{value:"Create the Asset Token",id:"create-the-asset-token",level:2},{value:"Check the Balance",id:"check-the-balance",level:2}],p={toc:c},u="wrapper";function h(e){let{components:t,...n}=e;return(0,a.kt)(u,(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"minting-an-asset"},"Minting an Asset"),(0,a.kt)("p",null,"In this tutorial we are going to mint an asset using the brambl-cli."),(0,a.kt)("p",null,"The process of minting a new asset is the following:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"First, we need to create a group constructor token."),(0,a.kt)("li",{parentName:"ul"},"Then, we need to create a series constructor token."),(0,a.kt)("li",{parentName:"ul"},"Finally, we need to create an asset constructor token using the group and series constructor tokens.")),(0,a.kt)("p",null,"Each one of these operations requires creating, proving and broadcasting\na transaction."),(0,a.kt)("h2",{id:"create-the-group-constructor-token"},"Create the Group Constructor Token"),(0,a.kt)("p",null,"The first step is to create the group constructor token. Before doing that,\nwe need to create a group policy. A group policy is a document that describes\nthe rules of the group. The group policy specifies the name of the group, and\nif the group is fixed or not. If the group is fixed, then the group constructor\ntokens can only be used with the series constructor tokens that have the same\nseries identifier that is fixed."),(0,a.kt)("p",null,"Group policies are simple text files in YAML format. The group policy file\nfor this tutorial looks like this:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-yaml"},"label: MyGroupPolicy\nregistrationUtxo: tv4zwbVos3RCB2x3r2PNbMU4PJANU7rGpine8dcjvZr#0\n")),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"label")," field is the name of the group. The ",(0,a.kt)("inlineCode",{parentName:"p"},"fixedSeries")," field is the\nseries identifier of the series constructor token that will be used to mint\nthe asset constructor token. The data in the ",(0,a.kt)("inlineCode",{parentName:"p"},"fixedSeries")," field is\n32 byte encoded in hexadecimal format. This field is optional."),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"registrationUtxo")," field is the utxo that will be used to register the\ngroup constructor token. This UTXO must contain at least 1 LVL and needs to\nbe spent in the minting transaction. Since each UTXO can only be spent once,\nthe group constructor token can only be registered once. The group contructor\ntoken identifier is the hash of the group policy file. To get the UTXO from an address we need to run the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli genus-query utxo-by-address --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE -h $HOST --port $PORT --walletdb $WALLET\n")),(0,a.kt)("p",null,"This will query the UXTOs for the address in the genus node. It uses the wallet to derive the right address to query."),(0,a.kt)("p",null,"The output will look something like this:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre"},"TxoAddress : 9xuuz3GWYWtFPxYWUg1v5KHpeCuhJfyZ2x2KzuxsVRLN#0\nLockAddress: ptetP7jshHTwEg9Fz9Xa1AmmzhYHDHo1zZRde7mnw3fddcXPjV14RPcgVgy7\nType       : LVL\nValue      : 10000000\n")),(0,a.kt)("p",null,"The TxoAddress is the UTXO that we need to use in the group policy file."),(0,a.kt)("p",null,"Once we have the group policy file, we can create the group constructor token\nusing the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli simple-minting create --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE  -h $HOST --port $PORT -n private --keyfile $KEYFILE -w $PASSWORD -o $MINTING_TX -i $GROUP_POLICY  -a $AMOUNT_TOKENS_TO_MINT --fee $FEE_AMOUNT --walletdb $WALLET_DB --mint-token group\n")),(0,a.kt)("p",null,"Then we need to prove and broadcast the transaction. This is the same procedure\nthat is used for all transaction and is showin in the how-tos ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/prove-simple-tx"},"Prove Transaction")," and ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/broadcast-tx"},"Broadcast Transaction"),"."),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."),(0,a.kt)("h2",{id:"create-the-series-constructor-token"},"Create the Series Constructor Token"),(0,a.kt)("p",null,"The next step is to create the series constructor token. To do that we need\nto create a series policy. A series policy is a document that describes the\nrules of the series. The series policy specifies:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"the name of the series"),(0,a.kt)("li",{parentName:"ul"},"the ",(0,a.kt)("inlineCode",{parentName:"li"},"registrationUtxo")," that will be used to register the series constructor token"),(0,a.kt)("li",{parentName:"ul"},"the ",(0,a.kt)("inlineCode",{parentName:"li"},"fungibility")," of the series"),(0,a.kt)("li",{parentName:"ul"},"the ",(0,a.kt)("inlineCode",{parentName:"li"},"quantityDescriptor")," of the series"),(0,a.kt)("li",{parentName:"ul"},"the permanent metadata schema of the series"),(0,a.kt)("li",{parentName:"ul"},"the ephemeral metadata schema of the series")),(0,a.kt)("p",null,"Series policies are simple text files in YAML format. The series policy file\nfor this tutorial looks like this (you need to replace the ",(0,a.kt)("inlineCode",{parentName:"p"},"registrationUtxo"),"\nfor the actual value):"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-yaml"},"label: MySeriesPolicy\nregistrationUtxo: 33HxStncsrptPB3ffkGpJNmoYwkkURvhiw92afWzjV3B#0\nfungibility: group-and-series\nquantityDescriptor: liquid\npermanentMetadata:\n  type: object\n  properties:\n    name:\n      type: string\n    tickerName:\n      type: string\n    description:\n      type: string\nephemeralMetadata:\n  type: object\n  properties:\n    url:\n      type: string\n    image:\n      type: string\n")),(0,a.kt)("p",null,"To create a simple minting transaction of series constructor tokens we run the\nfollowing command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli simple-minting create --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE  -h \n$HOST --port $PORT -n private --keyfile $KEYFILE -w $PASSWORD -o $MINTING_TX -i $SERIES_POLICY  -a $AMOUNT_TOKENS_TO_MINT --fee $FEE_AMOUNT --walletdb $WALLET_DB --mint-token series\n")),(0,a.kt)("p",null,"Then we need to prove and broadcast the transaction. This is the same procedure\nthat is used for all transaction and is showin in the how-tos ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/prove-simple-tx"},"Prove Transaction")," and ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/broadcast-tx"},"Broadcast Transaction"),"."),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."),(0,a.kt)("h2",{id:"create-the-asset-token"},"Create the Asset Token"),(0,a.kt)("p",null,"The next step is to create the actual asset. To do that we need to create an\nasset minting statement. An asset minting statement is a document that describes\nthe asset. The asset minting statement specifies:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"the UTXO that contains the group constructor token."),(0,a.kt)("li",{parentName:"ul"},"the UTXO that contains the series constructor token."),(0,a.kt)("li",{parentName:"ul"},"the quantity of tokens to mint.")),(0,a.kt)("p",null,"Asset minting statements are simple text files in YAML format. The asset minting\nstatement file for this tutorial looks like this (you need to replace the ",(0,a.kt)("inlineCode",{parentName:"p"},"groupTokenUtxo")," and ",(0,a.kt)("inlineCode",{parentName:"p"},"seriesTokenUtxo")," for the actual values):"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-yaml"},"groupTokenUtxo: 33HxStncsrptPB3ffkGpJNmoYwkkURvhiw92afWzjV3B#1\nseriesTokenUtxo: 33HxStncsrptPB3ffkGpJNmoYwkkURvhiw92afWzjV3B#2\nquantity: 1000\npermanentMetadata:\n  tickerName: TST\n  name: Test Token\n  description: Test Token Description\n")),(0,a.kt)("p",null,"To create a simple minting transaction of asset tokens we run the following\ncommand:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli simple-minting create --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE  -h $HOST --port $PORT -n private --keyfile $KEYFILE -w $PASSWORD -o $MINTING_TX -i $AMS --fee $FEE_AMOUNT --walletdb $WALLET_DB --mint-token asset --commitment $COMMITMENT --ephemeralMetadata $EPHEMERAL_METADATA_FILE\n")),(0,a.kt)("p",null,"Then we need to prove and broadcast the transaction. This is the same procedure\nthat is used for all transaction and is showin in the how-tos ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/prove-simple-tx"},"Prove Transaction")," and ",(0,a.kt)("a",{parentName:"p",href:"/docs/current/how-tos/broadcast-tx"},"Broadcast Transaction"),"."),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."),(0,a.kt)("h2",{id:"check-the-balance"},"Check the Balance"),(0,a.kt)("p",null,"You can check the balance of the address ",(0,a.kt)("inlineCode",{parentName:"p"},"$TO_ADDRESS")," using the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli wallet balance --from-address $TO_ADDRESS --walletdb $WALLET_DB --host $HOST --port $PORT\n")),(0,a.kt)("p",null,"You will see the asset, the group token and the series token."),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."))}h.isMDXComponent=!0}}]);