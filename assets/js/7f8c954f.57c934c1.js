"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[428],{3905:(e,r,t)=>{t.d(r,{Zo:()=>l,kt:()=>f});var n=t(7294);function o(e,r,t){return r in e?Object.defineProperty(e,r,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[r]=t,e}function a(e,r){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);r&&(n=n.filter((function(r){return Object.getOwnPropertyDescriptor(e,r).enumerable}))),t.push.apply(t,n)}return t}function s(e){for(var r=1;r<arguments.length;r++){var t=null!=arguments[r]?arguments[r]:{};r%2?a(Object(t),!0).forEach((function(r){o(e,r,t[r])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):a(Object(t)).forEach((function(r){Object.defineProperty(e,r,Object.getOwnPropertyDescriptor(t,r))}))}return e}function u(e,r){if(null==e)return{};var t,n,o=function(e,r){if(null==e)return{};var t,n,o={},a=Object.keys(e);for(n=0;n<a.length;n++)t=a[n],r.indexOf(t)>=0||(o[t]=e[t]);return o}(e,r);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(n=0;n<a.length;n++)t=a[n],r.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(o[t]=e[t])}return o}var i=n.createContext({}),c=function(e){var r=n.useContext(i),t=r;return e&&(t="function"==typeof e?e(r):s(s({},r),e)),t},l=function(e){var r=c(e.components);return n.createElement(i.Provider,{value:r},e.children)},d="mdxType",p={inlineCode:"code",wrapper:function(e){var r=e.children;return n.createElement(n.Fragment,{},r)}},y=n.forwardRef((function(e,r){var t=e.components,o=e.mdxType,a=e.originalType,i=e.parentName,l=u(e,["components","mdxType","originalType","parentName"]),d=c(t),y=o,f=d["".concat(i,".").concat(y)]||d[y]||p[y]||a;return t?n.createElement(f,s(s({ref:r},l),{},{components:t})):n.createElement(f,s({ref:r},l))}));function f(e,r){var t=arguments,o=r&&r.mdxType;if("string"==typeof e||o){var a=t.length,s=new Array(a);s[0]=y;var u={};for(var i in r)hasOwnProperty.call(r,i)&&(u[i]=r[i]);u.originalType=e,u[d]="string"==typeof e?e:o,s[1]=u;for(var c=2;c<a;c++)s[c]=t[c];return n.createElement.apply(null,s)}return n.createElement.apply(null,t)}y.displayName="MDXCreateElement"},5498:(e,r,t)=>{t.r(r),t.d(r,{assets:()=>i,contentTitle:()=>s,default:()=>p,frontMatter:()=>a,metadata:()=>u,toc:()=>c});var n=t(7462),o=(t(7294),t(3905));const a={sidebar_position:8},s="Query The Genus Node",u={unversionedId:"how-tos/genus-query",id:"how-tos/genus-query",title:"Query The Genus Node",description:"The Genus node provides a query mode to query the UXTOs of a given address.",source:"@site/docs/how-tos/genus-query.md",sourceDirName:"how-tos",slug:"/how-tos/genus-query",permalink:"/brambl-cli/docs/current/how-tos/genus-query",draft:!1,tags:[],version:"current",sidebarPosition:8,frontMatter:{sidebar_position:8},sidebar:"tutorialSidebar",previous:{title:"Query the Bifrost Node",permalink:"/brambl-cli/docs/current/how-tos/bifrost-query"},next:{title:"Manage Parties",permalink:"/brambl-cli/docs/current/how-tos/manage-parties"}},i={},c=[{value:"Query UXTO by address",id:"query-uxto-by-address",level:2}],l={toc:c},d="wrapper";function p(e){let{components:r,...t}=e;return(0,o.kt)(d,(0,n.Z)({},l,t,{components:r,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"query-the-genus-node"},"Query The Genus Node"),(0,o.kt)("p",null,"The Genus node provides a query mode to query the UXTOs of a given address. "),(0,o.kt)("h2",{id:"query-uxto-by-address"},"Query UXTO by address"),(0,o.kt)("p",null,"To query UXTOs by address run the following command:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli genus-query utxo-by-address --from-party $PARTY --from-contract $CONTRACT -h $HOST --port $PORT --walletdb $WALLET\n")),(0,o.kt)("p",null,"This will query the UXTOs for the address in the genus node. It uses the wallet to derive the right address to query."))}p.isMDXComponent=!0}}]);