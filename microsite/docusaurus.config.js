// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'brambl-cli',
  tagline: 'The Swiss Army Knife of the Apparatus Blockchain',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://topl.github.io/',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/brambl-cli',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'Topl', // Usually your GitHub org/user name.
  projectName: 'brambl-cli', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          lastVersion: 'current',
          versions: {
            current: {
              label: 'current',
              path: 'current',
              badge: true,
            },
          },
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        style: 'dark',
        title: '',
        logo: {
          alt: 'Brambll CLI Logo',
          src: 'img/logo.svg',
        },
        items: [
          {to: '/docs/current/intro', label: 'Getting Started', position: 'left'},
          {to: '/docs/current/category/concepts', label: 'Concepts', position: 'left'},
          {to: '/docs/current/category/tutorials', label: 'Tutorials', position: 'left'},
          {to: '/docs/current/category/how-tos', label: 'How Tos', position: 'left'},
          {to: '/docs/current/category/cli-reference', label: 'CLI Reference', position: 'left'},
          {
            href: 'https://github.com/Topl/brambl-cli',
            label: 'GitHub',
            position: 'right',
          },
        ], 
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Getting Started',
                to: '/docs/current/intro',
              },
              {
                label: 'Concepts',
                to: '/docs/current/category/concepts',
              },
              {
                label: 'Tutorials',
                to: '/docs/current/category/tutorials',
              },
              {
                label: 'How Tos',
                to: '/docs/current/category/how-tos',
              },
              {
                label: 'CLI Reference',
                to: '/docs/current/category/cli-reference',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Discord',
                href: 'https://discord.com/invite/Gp7fFq6Wck',
              },
              {
                label: 'Twitter',
                href: 'https://twitter.com/topl_protocol',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/Topl/brambl-cli',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Apparatus, LLC. Built with Docusaurus.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
