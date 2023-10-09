"use strict";(self.webpackChunkmicrosite=self.webpackChunkmicrosite||[]).push([[284],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>m});var r=n(7294);function o(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function a(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?a(Object(n),!0).forEach((function(t){o(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):a(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,r,o=function(e,t){if(null==e)return{};var n,r,o={},a=Object.keys(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||(o[n]=e[n]);return o}(e,t);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);for(r=0;r<a.length;r++)n=a[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(o[n]=e[n])}return o}var s=r.createContext({}),c=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},p=function(e){var t=c(e.components);return r.createElement(s.Provider,{value:t},e.children)},d="mdxType",u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},f=r.forwardRef((function(e,t){var n=e.components,o=e.mdxType,a=e.originalType,s=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),d=c(n),f=o,m=d["".concat(s,".").concat(f)]||d[f]||u[f]||a;return n?r.createElement(m,i(i({ref:t},p),{},{components:n})):r.createElement(m,i({ref:t},p))}));function m(e,t){var n=arguments,o=t&&t.mdxType;if("string"==typeof e||o){var a=n.length,i=new Array(a);i[0]=f;var l={};for(var s in t)hasOwnProperty.call(t,s)&&(l[s]=t[s]);l.originalType=e,l[d]="string"==typeof e?e:o,i[1]=l;for(var c=2;c<a;c++)i[c]=n[c];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}f.displayName="MDXCreateElement"},9725:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>i,default:()=>u,frontMatter:()=>a,metadata:()=>l,toc:()=>c});var r=n(7462),o=(n(7294),n(3905));const a={sidebar_position:12},i="Create a Complex Transaction",l={unversionedId:"how-tos/complex-tx",id:"how-tos/complex-tx",title:"Create a Complex Transaction",description:"To create a transaction from a file run the following command:",source:"@site/docs/how-tos/complex-tx.md",sourceDirName:"how-tos",slug:"/how-tos/complex-tx",permalink:"/brambl-cli/docs/current/how-tos/complex-tx",draft:!1,tags:[],version:"current",sidebarPosition:12,frontMatter:{sidebar_position:12},sidebar:"tutorialSidebar",previous:{title:"Manage Keys",permalink:"/brambl-cli/docs/current/how-tos/manage-keys"},next:{title:"Mint a Group Constructor Token",permalink:"/brambl-cli/docs/current/how-tos/mint-group"}},s={},c=[{value:"Example of format",id:"example-of-format",level:2}],p={toc:c},d="wrapper";function u(e){let{components:t,...n}=e;return(0,o.kt)(d,(0,r.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,o.kt)("h1",{id:"create-a-complex-transaction"},"Create a Complex Transaction"),(0,o.kt)("p",null,"To create a transaction from a file run the following command:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-bash"},"brambl-cli tx create -i $INPUT_FILE --bifrost-port 9084 -o $OUTPUT_FILE -n private -h localhost\n")),(0,o.kt)("p",null,"This will create a transaction from the file ",(0,o.kt)("inlineCode",{parentName:"p"},"$INPUT_FILE")," and store the result in the file ",(0,o.kt)("inlineCode",{parentName:"p"},"$OUTPUT_FILE"),"."),(0,o.kt)("h2",{id:"example-of-format"},"Example of format"),(0,o.kt)("p",null,"A file to move the input from a height lock contract to a new address would look like this:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-yaml"},"network: private\n\nkeys: []\n\ninputs:\n  - address: 7exK7vSMd6aCYqiiZ1VjWSYLif98zHxsQgtqaRM3WvAc#1\n    keyMap: []\n    proposition: threshold(1, height(1, 9223372036854775807))\n    value: 10000000\noutputs:\n  - address: ptetP7jshHUxEn3noNHnfU5AhV8AcifVAWkhYYvXvrjfErEsey686BBukpQm\n    value: 10000000\n")),(0,o.kt)("p",null,"A file to move the input from a single signature lock contract to multiple addresses would look like this:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-yaml"},"network: private\n\nkeys: \n  - id: alice\n    vk: GeMD3jTdwPEpABPksjFYGgU9tLebpTbqiEvwF7Yyi5jHUBUXhtfsMRUVc5zE6fbL8FYrTDVNRt7eWwrbQMZuwswVP1zpWq8X8r\n\ninputs:\n  - address: 6YKBJePhf48a2kCdrov69v9NoiFAuLT7isthazhVBuu1#0\n    keyMap:\n     - index: 0\n       identifier: alice\n    proposition: threshold(1, sign(0))\n    value: 10000000\noutputs:\n  - address: ptetP7jshHUxEn3noNHnfU5AhV8AcifVAWkhYYvXvrjfErEsey686BBukpQm \n    value: 9998000\n  - address: ptetP7jshHV4h1qe2uSoaKYiM618wKHbpjwadc7HxEGiDQ85MCMzoFF6oh6R \n    value: 1000\n  - address: ptetP7jshHUiwiZvm48Z1ebrARr36cxAKTKfhETfRKXomouZafiRadz2m7jp\n    value: 1000\n")),(0,o.kt)("p",null,"A file to move two different UTXOs protected by different locks and different\nsignatures to a single address would look like this:"),(0,o.kt)("pre",null,(0,o.kt)("code",{parentName:"pre",className:"language-yaml"},"network: private\n\nkeys: \n  - id: aliceAnd\n    vk: GeMD3jTedVsewW98Cin4Ksgtce784bwpnpcGZDw9wT9WxCaq3PFKo7zRGzaY6ycCcZeB7Sibdsi8DYdYR3u8go9C6W8Wq6K2TS\n  - id: bobAnd\n    vk: GeMD3jTdf9P5qLDw8PLzSJR77jXVcPBZfZVKYMWdZfr9urzDPvCemfdLfRHSNPUqL9hVokQTK4eYfVki5bLAtfoEFbeTU61zAY\n  - id: aliceOr\n    vk: GeMD3jTejkvMtBxhrZo3cgDv7g8tUaJ8QaXJocaz9jS5Re4faHQYhU6RDoimtXUwuFGcMccp4jPdJHY6R3GeKpZ5VvHF25cin3\n\ninputs:\n  - address: 3WiAub289RrnFA5rdr5wouTdEbqoef2rHWe6edygXeUL#1\n    keyMap:\n     - index: 0\n       identifier: aliceAnd\n     - index: 1\n       identifier: bobAnd\n    proposition: threshold(1, sign(0) and sign(1))\n    value: 1000\n  - address: 3WiAub289RrnFA5rdr5wouTdEbqoef2rHWe6edygXeUL#2\n    keyMap:\n     - index: 0\n       identifier: aliceOr\n     - index: 1\n       identifier: aliceOr\n    proposition: threshold(1, sign(0) or sign(1))\n    value: 1000\noutputs:\n  - address: ptetP7jshHUaBVrsqnn3bhtWQ1kugVGJVYsVS4WZ47AthWeL4ZX9B9ZJNTaw \n    value: 2000\n")))}u.isMDXComponent=!0}}]);