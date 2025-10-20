export enum MusicGenre {
  PROGRESSIVE_ROCK = 'PROG ROCK',
  SOUL = 'SOUL',
  ROCK = 'ROCK',
  POST_ROCK = 'POST ROCK',
  PUNK_ROCK = 'PUNK ROCK',
  POST_PUNK = 'POST PUNK'
}

export function parseMusicGenre(genreString: string): MusicGenre {
  const genreMap: {[key: string]: MusicGenre} = {
    'PROGRESSIVE_ROCK': MusicGenre.PROGRESSIVE_ROCK,
    'ROCK': MusicGenre.ROCK,
    'SOUL': MusicGenre.SOUL,
    'POST_ROCK': MusicGenre.POST_ROCK,
    'PUNK_ROCK': MusicGenre.PUNK_ROCK,
    'POST_PUNK': MusicGenre.POST_PUNK
  };

  return genreMap[genreString];
}

export function getMusicGenreOptions(): { value: string; label: string }[] {
  return [
    { value: 'PROGRESSIVE_ROCK', label: 'PROG ROCK' },
    { value: 'SOUL', label: 'SOUL' },
    { value: 'ROCK', label: 'ROCK' },
    { value: 'POST_ROCK', label: 'POST ROCK' },
    { value: 'PUNK_ROCK', label: 'PUNK ROCK' },
    { value: 'POST_PUNK', label: 'POST PUNK' }
  ];
}
