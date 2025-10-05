/** @type {import('tailwindcss').Config} */
module.exports = {
  theme: {
    extend: {
      keyframes: {
        slideRight: {
          '0%': { width: '0%' },
          '100%': { width: '100%' },
        },
      },
      animation: {
        slideRight: 'slideRight 1s ease-in-out forwards',
      },
    },
  },
  plugins: [],
};
