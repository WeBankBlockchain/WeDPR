export function spliceLegendHome(legendData, color = 'white') {
  return {
    data: legendData,
    left: 'center',
    bottom: '4px',
    icon: 'circle',
    orient: 'horizontal', // vertical
    itemWidth: 8,
    itemHeight: 8,
    formatter: (name) => {
      return `{b|${name}} `
    },
    x: 'center',
    textStyle: {
      color,
      fontSize: 10,
      align: 'left',
      // 文字块背景色，一定要加上，否则对齐不会生效
      backgroundColor: 'transparent',
      rich: {
        b: {
          width: 94,
          lineHeight: 10
        }
      }
    }
  }
}
