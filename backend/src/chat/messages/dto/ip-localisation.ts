import fetch from 'node-fetch';

export async function getLocationFromIp(ip: string): Promise<{ lat: number; lon: number }> {
    // if (ip != "127.0.0.1") {
    try {
        const res = await fetch(`http://ip-api.com/json/${ip}`);
        const data = await res.json() as {
            status: string;
            lat: number;
            lon: number;
        };

        //console.log(`Geolocalization: ${data.status} ${data.lat} ${data.lon}`);
        if (data.status === "success") {
            return {
                lat: data.lat,
                lon: data.lon
            };
        }
    } catch (e) {
        console.error("Erreur localisation IP:", e);
    }
    // }

    return { lat: 0, lon: 0 }; // fallback safe value
}