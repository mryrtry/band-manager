export enum Country {
  FRANCE = 'FRANCE',
  INDIA = 'INDIA',
  THAILAND = 'THAILAND',
  USA = 'USA',
  UK = 'UK'
}

export function parseCountry(genreString: string): Country {
  const countryMap: { [key: string]: Country } = {
    'FRANCE': Country.FRANCE,
    'INDIA': Country.INDIA,
    'THAILAND': Country.THAILAND,
    'USA': Country.USA,
    'UK': Country.UK,
  };

  return countryMap[genreString];
}
