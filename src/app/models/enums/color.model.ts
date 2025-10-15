export enum Color {
  BLACK = 'BLACK',
  ORANGE = 'ORANGE',
  BROWN = 'BROWN',
  GREEN = 'GREEN',
  BLUE = 'BLUE'
}

export function parseColor(genreString: string): Color {
  const colorMap: { [key: string]: Color } = {
    'BLACK': Color.BLACK,
    'ORANGE': Color.ORANGE,
    'BROWN': Color.BROWN,
    'GREEN': Color.GREEN,
    'BLUE': Color.BLUE,
  };

  return colorMap[genreString];
}
