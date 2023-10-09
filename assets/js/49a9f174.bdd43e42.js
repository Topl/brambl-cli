"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[995],{3905:(e,t,r)=>{r.d(t,{Zo:()=>p,kt:()=>f});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function c(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function a(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?c(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):c(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function i(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},c=Object.keys(e);for(n=0;n<c.length;n++)r=c[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var c=Object.getOwnPropertySymbols(e);for(n=0;n<c.length;n++)r=c[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var l=n.createContext({}),s=function(e){var t=n.useContext(l),r=t;return e&&(r="function"==typeof e?e(t):a(a({},t),e)),r},p=function(e){var t=s(e.components);return n.createElement(l.Provider,{value:t},e.children)},u="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},m=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,c=e.originalType,l=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),u=s(r),m=o,f=u["".concat(l,".").concat(m)]||u[m]||d[m]||c;return r?n.createElement(f,a(a({ref:t},p),{},{components:r})):n.createElement(f,a({ref:t},p))}));function f(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var c=r.length,a=new Array(c);a[0]=m;var i={};for(var l in t)hasOwnProperty.call(t,l)&&(i[l]=t[l]);i.originalType=e,i[u]="string"==typeof e?e:o,a[1]=i;for(var s=2;s<c;s++)a[s]=r[s];return n.createElement.apply(null,a)}return n.createElement.apply(null,r)}m.displayName="MDXCreateElement"},3748:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>l,contentTitle:()=>a,default:()=>d,frontMatter:()=>c,metadata:()=>i,toc:()=>s});var n=r(7462),o=(r(7294),r(3905));const c={sidebar_position:2},a="Contract Mode",i={unversionedId:"cli-reference/contract-mode",id:"cli-reference/contract-mode",title:"Contract Mode",description:"",source:"@site/docs/cli-reference/contract-mode.md",sourceDirName:"cli-reference",slug:"/cli-reference/contract-mode",permalink:"/brambl-cli/docs/current/cli-reference/contract-mode",draft:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Entity Mode",permalink:"/brambl-cli/docs/current/cli-reference/entity-mode"},next:{title:"Genus Query Mode",permalink:"/brambl-cli/docs/current/cli-reference/genus-query-mode"}},l={},s=[],p={toc:s},u="wrapper";function d(e){let{components:t,...r}=e;return(0,o.kt)(u,(0,n.Z)({},p,r,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"contract-mode"},"Contract Mode"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"Command: contracts [list|add] [options]\nContract mode\nCommand: contracts list\nList existing contracts\n  --walletdb <value>       Wallet DB file. (mandatory)\nCommand: contracts add\nAdd a new contracts\n  --walletdb <value>       Wallet DB file. (mandatory)\n  --contract-name <value>  Name of the contract. (mandatory)\n  --contract-template <value>\n                           Contract template. (mandatory)\n")))}d.isMDXComponent=!0}}]);