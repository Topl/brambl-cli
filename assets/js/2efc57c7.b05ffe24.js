"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[355],{3905:(e,t,r)=>{r.d(t,{Zo:()=>p,kt:()=>f});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function a(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function i(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?a(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):a(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function c(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)r=a[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var l=n.createContext({}),s=function(e){var t=n.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):i(i({},t),e)),r},p=function(e){var t=s(e.components);return n.createElement(l.Provider,{value:t},e.children)},m="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},u=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,a=e.originalType,l=e.parentName,p=c(e,["components","mdxType","originalType","parentName"]),m=s(r),u=o,f=m["".concat(l,".").concat(u)]||m[u]||d[u]||a;return r?n.createElement(f,i(i({ref:t},p),{},{components:r})):n.createElement(f,i({ref:t},p))}));function f(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=r.length,i=new Array(a);i[0]=u;var c={};for(var l in t)hasOwnProperty.call(t,l)&&(c[l]=t[l]);c.originalType=e,c[m]="string"==typeof e?e:o,i[1]=c;for(var s=2;s<a;s++)i[s]=r[s];return n.createElement.apply(null,i)}return n.createElement.apply(null,r)}u.displayName="MDXCreateElement"},563:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>i,default:()=>d,frontMatter:()=>a,metadata:()=>c,toc:()=>s});var n=r(7462),o=(r(7294),r(3905));const a={sidebar_position:7},i="Simple Transaction Mode",c={unversionedId:"cli-reference/simple-tx-mode",id:"cli-reference/simple-tx-mode",title:"Simple Transaction Mode",description:"",source:"@site/docs/cli-reference/simple-tx-mode.md",sourceDirName:"cli-reference",slug:"/cli-reference/simple-tx-mode",permalink:"/brambl-cli/docs/current/cli-reference/simple-tx-mode",draft:!1,tags:[],version:"current",sidebarPosition:7,frontMatter:{sidebar_position:7},sidebar:"tutorialSidebar",previous:{title:"Transaction Mode",permalink:"/brambl-cli/docs/current/cli-reference/transaction-mode"},next:{title:"Simple Minting Mode",permalink:"/brambl-cli/docs/current/cli-reference/simple-minting.mode"}},l={},s=[],p={toc:s},m="wrapper";function d(e){let{components:t,...r}=e;return(0,o.kt)(m,(0,n.Z)({},p,r,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"simple-transaction-mode"},"Simple Transaction Mode"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"Command: simple-transaction [create] [options]\nSimple transaction mode\nCommand: simple-transaction create\nCreate transaction\n  --from-party <value>     Party where we are sending the funds from\n  --from-contract <value>  Contract where we are sending the funds from\n  --from-state <value>     State from where we are sending the funds from\n  -n, --network <value>    Network name: Possible values: mainnet, testnet, private. (mandatory)\n  -h, --host <value>       The host of the node. (mandatory)\n  --port <value>   Port Bifrost node. (mandatory)\n  -k, --keyfile <value>    The key file.\n  -w, --password <value>   Password for the encrypted key. (mandatory)\n  --walletdb <value>       Wallet DB file. (mandatory)\n  -o, --output <value>     The output file. (mandatory)\n  -t, --to <value>         Address to send LVLs to. (mandatory if to-party and to-contract are not provided)\n  --to-party <value>       Party to send LVLs to. (mandatory if to is not provided)\n  --to-contract <value>    Contract to send LVLs to. (mandatory if to is not provided)\n  -a, --amount <value>     Amount to send simple transaction\n")))}d.isMDXComponent=!0}}]);