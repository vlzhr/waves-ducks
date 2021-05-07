const claimerSeed = '';

describe('some suite', () => {

    it('start auction', async () => {
        const invokeTx1 = invokeScript({
            dApp: "3P9qnTEskEDPDwjC9UtnaFGLvobSPbeCHx6",
            fee: 500000,
            call: {
                function: "startDuckBreeding"
            },
            payment: [{
                assetId: "5KTSQ9FFiEdtJkBcLqmGGogKDquy28jWhk56fKoBQpf5",
                amount: 1
            }, {
                assetId: "4G4gQmAuboMQiVJ6oug61YwxZGXnWT9P4cKo7p7FRSeG",
                amount: 1
            }]
        }, claimerSeed);
        await broadcast(invokeTx1, "https://nodes.wavesnodes.com");
        console.log('Transaction:', invokeTx1);
        await waitForTx(invokeTx1.id);
        console.log('Transaction is in the blockchain: ', invokeTx1.id);
    })

})
