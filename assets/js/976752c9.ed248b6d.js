"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[9841],{3905:(e,t,r)=>{r.d(t,{Zo:()=>p,kt:()=>m});var n=r(7294);function o(e,t,r){return t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}function i(e,t){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),r.push.apply(r,n)}return r}function l(e){for(var t=1;t<arguments.length;t++){var r=null!=arguments[t]?arguments[t]:{};t%2?i(Object(r),!0).forEach((function(t){o(e,t,r[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(r,t))}))}return e}function a(e,t){if(null==e)return{};var r,n,o=function(e,t){if(null==e)return{};var r,n,o={},i=Object.keys(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||(o[r]=e[r]);return o}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)r=i[n],t.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(o[r]=e[r])}return o}var c=n.createContext({}),s=function(e){var t=n.useContext(c),r=t;return e&&(r="function"==typeof e?e(t):l(l({},t),e)),r},p=function(e){var t=s(e.components);return n.createElement(c.Provider,{value:t},e.children)},f="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return n.createElement(n.Fragment,{},t)}},u=n.forwardRef((function(e,t){var r=e.components,o=e.mdxType,i=e.originalType,c=e.parentName,p=a(e,["components","mdxType","originalType","parentName"]),f=s(r),u=o,m=f["".concat(c,".").concat(u)]||f[u]||d[u]||i;return r?n.createElement(m,l(l({ref:t},p),{},{components:r})):n.createElement(m,l({ref:t},p))}));function m(e,t){var r=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var i=r.length,l=new Array(i);l[0]=u;var a={};for(var c in t)hasOwnProperty.call(t,c)&&(a[c]=t[c]);a.originalType=e,a[f]="string"==typeof e?e:o,l[1]=a;for(var s=2;s<i;s++)l[s]=r[s];return n.createElement.apply(null,l)}return n.createElement.apply(null,r)}u.displayName="MDXCreateElement"},6988:(e,t,r)=>{r.r(t),r.d(t,{assets:()=>c,contentTitle:()=>l,default:()=>d,frontMatter:()=>i,metadata:()=>a,toc:()=>s});var n=r(7462),o=(r(7294),r(3905));const i={sidebar_position:1},l="Fellowships Mode",a={unversionedId:"cli-reference/entity-mode",id:"cli-reference/entity-mode",title:"Fellowships Mode",description:"",source:"@site/docs/cli-reference/entity-mode.md",sourceDirName:"cli-reference",slug:"/cli-reference/entity-mode",permalink:"/brambl-cli/docs/current/cli-reference/entity-mode",draft:!1,tags:[],version:"current",sidebarPosition:1,frontMatter:{sidebar_position:1},sidebar:"tutorialSidebar",previous:{title:"CLI Reference",permalink:"/brambl-cli/docs/current/category/cli-reference"},next:{title:"Template Mode",permalink:"/brambl-cli/docs/current/cli-reference/contract-mode"}},c={},s=[],p={toc:s},f="wrapper";function d(e){let{components:t,...r}=e;return(0,o.kt)(f,(0,n.Z)({},p,r,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"fellowships-mode"},"Fellowships Mode"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"Command: fellowships [list|add]\nFellowship mode\nCommand: fellowships list [options]\nList existing fellowships\n  --walletdb <value>       Wallet DB file. (mandatory)\nCommand: fellowships add [options]\nAdd a new fellowships\n  --walletdb <value>       Wallet DB file. (mandatory)\n  --fellowship-name <value>\n                           Name of the fellowship. (mandatory)\n")))}d.isMDXComponent=!0}}]);