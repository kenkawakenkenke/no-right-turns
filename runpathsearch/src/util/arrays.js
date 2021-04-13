
// Given an array:
//  [salmon, cod, bear]
// and a key function
//  keyFunc = return animal or fish
// converts to a multimap
//  {
//   animal: [bear],
//   fish: [salmon, cod]
//  }
// Optionally converting the value using valueFunc
export function toMultimap(array, keyFunc, valueFunc = (a => a)) {
    return array.reduce((accum, c) => {
        const key = keyFunc(c);
        return {
            ...accum,
            [key]: [
                ...(accum[key] || []),
                valueFunc(c),
            ],
        };
    }, {});
};

// Given an array:
//  [salmon, cod, bear]
// and a key function
//  keyFunc = return animal or fish
// converts to a map
//  {
//   animal: bear,
//   fish: cod
//  }
// Optionally converting the value using valueFunc
export function toMap(array, keyFunc, valueFunc = (a => a)) {
    return array.reduce((accum, c) => ({
        ...accum,
        [keyFunc(c)]: valueFunc(c),
    }), {});
};

export function uniquify(array, keyFunc) {
    return Object.values(toMap(array, keyFunc));
}