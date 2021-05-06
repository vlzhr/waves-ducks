const wvs = 1e8; 
const eggId = '53bLXGQhnMxAoiWnza2SJQRp7hNQZVHnyrRziLmwdVjb';
const incubatorSeed = '';
const claimerSeed = '';
const incubatorAddress = '';
const scriptText = file("ducks-incubator.ride");
const compiledContract = compile(scriptText);

let hatchingTxId = ""

describe('some suite', () => {

    it('deploy script', async () => {
        const setScriptTx = setScript({
            script: compiledContract
        }, incubatorSeed)
    })

    it('claim hatching', async () => {
        const invokeTx1 = invokeScript({
            dApp: incubatorAddress,
            fee: 500000,
            call: {
                function: "startDuckHatching"
            },
            payment: [{
                assetId: eggId,
                amount: 1000
            }]
        }, claimerSeed);
        await broadcast(invokeTx1, "https://nodes.wavesnodes.com");
        console.log('Transaction:', invokeTx1);
        await waitForTx(invokeTx1.id);
        console.log('Transaction is in the blockchain: ', invokeTx1.id);
        hatchingTxId = invokeTx1.id;
    })

    it('finish hatching', async () => {
        const invokeTx = invokeScript({
            dApp: incubatorAddress,
            fee: 500000,
            call: {
                function: "finishDuckHatching",
                args: [{
                    type: 'string',
                    value: hatchingTxId
                }]
            },
            //payment: [{
            //    assetId: "",
            //    amount: 1000
            //}]
        }, claimerSeed);
        await broadcast(invokeTx, "https://nodes.wavesnodes.com");
        console.log('Transaction:', invokeTx);
        await waitForTx(invokeTx.id);
        console.log('Transaction is in the blockchain: ', invokeTx.id);
    })
})
