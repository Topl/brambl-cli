"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[919],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>m});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var s=r.createContext({}),c=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},p=function(e){var t=c(e.components);return r.createElement(s.Provider,{value:t},e.children)},u="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},h=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=c(n),h=a,m=u["".concat(s,".").concat(h)]||u[h]||d[h]||o;return n?r.createElement(m,i(i({ref:t},p),{},{components:n})):r.createElement(m,i({ref:t},p))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=h;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l[u]="string"==typeof e?e:a,i[1]=l;for(var c=2;c<o;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}h.displayName="MDXCreateElement"},6782:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>i,default:()=>d,frontMatter:()=>o,metadata:()=>l,toc:()=>c});var r=n(7462),a=(n(7294),n(3905));const o={sidebar_position:3},i="Creating Transactions",l={unversionedId:"tutorials/create-tx",id:"tutorials/create-tx",title:"Creating Transactions",description:"In this tutorial we are creating a transaction using the brambl-cli and",source:"@site/docs/tutorials/create-tx.md",sourceDirName:"tutorials",slug:"/tutorials/create-tx",permalink:"/brambl-cli/docs/current/tutorials/create-tx",draft:!1,tags:[],version:"current",sidebarPosition:3,frontMatter:{sidebar_position:3},sidebar:"tutorialSidebar",previous:{title:"Funding Your Wallet",permalink:"/brambl-cli/docs/current/tutorials/fund-wallet"},next:{title:"Minting an Asset",permalink:"/brambl-cli/docs/current/tutorials/mint-asset"}},s={},c=[{value:"Create an LVL transaction",id:"create-an-lvl-transaction",level:2},{value:"Prove the Transaction",id:"prove-the-transaction",level:2},{value:"Send the Transaction to the Network",id:"send-the-transaction-to-the-network",level:2},{value:"Check the Balance",id:"check-the-balance",level:2}],p={toc:c},u="wrapper";function d(e){let{components:t,...n}=e;return(0,a.kt)(u,(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("h1",{id:"creating-transactions"},"Creating Transactions"),(0,a.kt)("p",null,"In this tutorial we are creating a transaction using the brambl-cli and\nsending it to the network."),(0,a.kt)("p",null,"The process of creating a transaction is the following:"),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"First, we need to create the actual transaction."),(0,a.kt)("li",{parentName:"ul"},"Then, we need to prove the transaction."),(0,a.kt)("li",{parentName:"ul"},"Finally, we need to send the transaction to the network.")),(0,a.kt)("h2",{id:"create-an-lvl-transaction"},"Create an LVL transaction"),(0,a.kt)("p",null,"To create a simple transaction you need to run the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli simple-transaction create --from-fellowship $FELLOWSHIP --from-template $LOCK_TEMPLATE --from-interaction $INTERACTION_NR -t $TO_ADDRESS -w $PASSWORD --port $PORT -o $TX_FILE -n $NETWORK -a $SEND_AMOUNT -h $HOST -i $MAIN_KEY --walletdb $WALLET --fee $FEE --transfer-token $TOKEN_TYPE\n")),(0,a.kt)("p",null,"This will create a transaction that spends the interaction ",(0,a.kt)("inlineCode",{parentName:"p"},"$INTERACTION_NR")," of the template ",(0,a.kt)("inlineCode",{parentName:"p"},"$LOCK_TEMPLATE")," of the fellowship ",(0,a.kt)("inlineCode",{parentName:"p"},"$FELLOWSHIP")," and sends ",(0,a.kt)("inlineCode",{parentName:"p"},"$SEND_AMOUNT")," polys to the address ",(0,a.kt)("inlineCode",{parentName:"p"},"$TO_ADDRESS"),". The transaction will be stored in the file ",(0,a.kt)("inlineCode",{parentName:"p"},"$TX_FILE"),"."),(0,a.kt)("p",null,"The ",(0,a.kt)("inlineCode",{parentName:"p"},"--from-interaction")," parameter is only required if the fellowship is ",(0,a.kt)("inlineCode",{parentName:"p"},"nofellowship"),". If the fellowship is ",(0,a.kt)("inlineCode",{parentName:"p"},"self"),", or any template where there is at least one fellowship, then the ",(0,a.kt)("inlineCode",{parentName:"p"},"--from-interaction")," parameter is not required."),(0,a.kt)("h2",{id:"prove-the-transaction"},"Prove the Transaction"),(0,a.kt)("p",null,"To prove the transaction run the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli tx prove -w $PASSWORD --keyfile $MAIN_KEY -n $NETWORK -i $TX_FILE -o $TX_PROVED_FILE --walletdb $WALLET\n")),(0,a.kt)("p",null,"This will prove the transaction in the file ",(0,a.kt)("inlineCode",{parentName:"p"},"$TX_FILE")," and store the result in the file ",(0,a.kt)("inlineCode",{parentName:"p"},"$TX_PROVED_FILE"),". The right indexes to derive the keys are taken from the wallet database."),(0,a.kt)("h2",{id:"send-the-transaction-to-the-network"},"Send the Transaction to the Network"),(0,a.kt)("p",null,"To send the transaction to the network you need to run the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli tx broadcast -n $NETWORK -i $TX_PROVED_FILE -h $HOST --port $PORT --walletdb $WALLET\n")),(0,a.kt)("p",null,"This will broadcast the transaction in the file ",(0,a.kt)("inlineCode",{parentName:"p"},"$TX_PROVED_FILE")," to the network."),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."),(0,a.kt)("h2",{id:"check-the-balance"},"Check the Balance"),(0,a.kt)("p",null,"You can check the balance of the address ",(0,a.kt)("inlineCode",{parentName:"p"},"$TO_ADDRESS")," using the following command:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli wallet balance --from-address $TO_ADDRESS --walletdb $WALLET_DB --host $HOST --port $PORT\n")),(0,a.kt)("p",null,"Do not forget to use the ",(0,a.kt)("inlineCode",{parentName:"p"},"--secure")," parameter if you are using the testnet."))}d.isMDXComponent=!0}}]);