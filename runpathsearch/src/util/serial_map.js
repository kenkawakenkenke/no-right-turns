export default function serialMap(elements, elementToPromiseFactory) {
    let chain = Promise.resolve([]);
    elements.forEach(element => {
        chain = chain
            .then(chainedRes =>
                elementToPromiseFactory(element).then(res => [...chainedRes, res]));
    });
    return chain;
};
