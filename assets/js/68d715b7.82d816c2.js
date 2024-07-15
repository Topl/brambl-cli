"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[3896],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>f});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function l(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function a(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?l(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):l(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function i(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},l=Object.keys(e);for(r=0;r<l.length;r++)n=l[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(r=0;r<l.length;r++)n=l[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var c=r.createContext({}),s=function(e){var t=r.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):a(a({},t),e)),n},p=function(e){var t=s(e.components);return r.createElement(c.Provider,{value:t},e.children)},u="mdxType",d={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},h=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,l=e.originalType,c=e.parentName,p=i(e,["components","mdxType","originalType","parentName"]),u=s(n),h=o,f=u["".concat(c,".").concat(h)]||u[h]||d[h]||l;return n?r.createElement(f,a(a({ref:t},p),{},{components:n})):r.createElement(f,a({ref:t},p))}));function f(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var l=n.length,a=new Array(l);a[0]=h;var i={};for(var c in t)hasOwnProperty.call(t,c)&&(i[c]=t[c]);i.originalType=e,i[u]="string"==typeof e?e:o,a[1]=i;for(var s=2;s<l;s++)a[s]=n[s];return r.createElement.apply(null,a)}return r.createElement.apply(null,n)}h.displayName="MDXCreateElement"},4511:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>c,contentTitle:()=>a,default:()=>d,frontMatter:()=>l,metadata:()=>i,toc:()=>s});var r=n(7462),o=(n(7294),n(3905));const l={sidebar_position:2},a="Locks",i={unversionedId:"concepts/locks",id:"concepts/locks",title:"Locks",description:"All funds in the Apparatus blockchain are protected by a lock proposition. The lock",source:"@site/docs/concepts/locks.md",sourceDirName:"concepts",slug:"/concepts/locks",permalink:"/brambl-cli/docs/current/concepts/locks",draft:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{sidebar_position:2},sidebar:"tutorialSidebar",previous:{title:"Fellowships",permalink:"/brambl-cli/docs/current/concepts/fellowships"},next:{title:"Conversation",permalink:"/brambl-cli/docs/current/concepts/conversations"}},c={},s=[{value:"Lock Templates",id:"lock-templates",level:2}],p={toc:s},u="wrapper";function d(e){let{components:t,...n}=e;return(0,o.kt)(u,(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"locks"},"Locks"),(0,o.kt)("p",null,"All funds in the Apparatus blockchain are protected by a lock proposition. The lock\nproposition is not available to the users, only the lock address is."),(0,o.kt)("p",null,"To prove that they may use the funds in an address, users must provide both\nthe original lock proposition that was used to lock the funds and a proof that\nthey are the owners of the address."),(0,o.kt)("h2",{id:"lock-templates"},"Lock Templates"),(0,o.kt)("p",null,"A lock template is a Quivr expression where there are placeholders instead\nof the public keys of the fellows. The fellows fill the placeholders with\ntheir public keys to create a lock proposition."),(0,o.kt)("p",null,"For example, let us suppose that Alice and Bob want to lock funds together\nand they have already formed a fellowship. They can create a lock template\nwith the following Quivr expression:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre"},"and(sign(0), sign(1))\n")),(0,o.kt)("p",null,"The ",(0,o.kt)("inlineCode",{parentName:"p"},"sign(n)")," expression means that the ",(0,o.kt)("inlineCode",{parentName:"p"},"nth")," participan of the fellowship\nmust provide a public key to create the lock proposition. In this case,\nAlice must provide the public key to fill the placeholder ",(0,o.kt)("inlineCode",{parentName:"p"},"sign(0)")," and Bob must\nprovide the public key to fill the placeholder ",(0,o.kt)("inlineCode",{parentName:"p"},"sign(1)"),"."))}d.isMDXComponent=!0}}]);