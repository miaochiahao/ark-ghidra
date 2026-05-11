export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3, param_4, param_5)     {
        v2 = v10;
        v2.r = v11;
        v7 = v2;
        v7.g = v12;
        v7 = v2;
        v7.b = v13;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static gcd(param_0, param_1, param_2, param_3, param_4): void     {
        const v5 = v3;
        const v6 = v4;
        while (!(v8 !== 0)) {
            const v8 = v6;
            const v9 = v5;
            v6 %= v9;
            const v5 = v8;
            continue;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static testGCD(param_0, param_1, param_2): void     {
        let v7 = lex_0_0;
        v7 = v7(48, 18);
        v7 = lex_0_0;
        v8 = 100;
        v9 = 75;
        v7 = v7(v8, v9);
        v7 = lex_0_0;
        v8 = 17;
        v9 = 13;
        v7 = v7(v8, v9);
        let v11 = String;
        v11 = v11(v7);
        v9 = `,${v11}`;
        v11 = String;
        v8 = v7(v7) + v9;
        v7 = `,${v8}`;
        v8 = String;
        return v8(v7) + v7;
    }

    static toHex(param_0, param_1, param_2): void     {
        let v3 = "#";
        v3 = lex_0_2(v10.r) + v3;
        v6 = lex_0_2;
        r = v10;
        v3 = v6(r.g) + v3;
        v6 = lex_0_2;
        g = v10;
        v3 = v6(g.b) + v3;
        return v3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_6 = func_0;
        v11 = func_1;
        lex_0_1 = v11;
        v11 = func_2;
        lex_0_0 = v11;
        v11 = func_3;
        lex_0_4 = v11;
        v11 = func_9;
        lex_0_5 = v11;
        v11 = func_12;
        lex_0_2 = v11;
    }

    static testColor(param_0, param_1, param_2): void     {
        let v9 = 0xFF;
        let v10 = 0;
        let v11 = 0;
        const v6 = new 0();
        v9 = 0;
        v10 = 0xFF;
        v11 = 0;
        const v3 = new 0(v8, v9);
        v9 = v6;
        let toHex = v9.toHex;
        toHex = v9.toHex();
        v9 = v3;
        toHex = v9.toHex;
        toHex = v9.toHex();
        v9 = toHex;
        toHex = `,${v9}`;
        return toHex + toHex;
    }

    static toHexByte(param_0, param_1, param_2, param_3): void     {
        let v5 = "";
        const v9 = v3;
        const v6: number = 16 / v9;
        const v10 = Math;
        let floor = v10.floor;
        floor = v10.floor(v6);
        floor = v16;
        const v7: number = 16 % floor;
        const v4 = "0123456789abcdef";
        floor = v5;
        v11 = v4;
        v5 = v11.charAt(floor) + floor;
        floor = v5;
        charAt = v4.charAt;
        v12 = v7;
        v5 = v4.charAt(v12) + floor;
        return v5;
    }

    static testGuards(param_0, param_1, param_2): void     {
        let v8 = lex_0_5;
        v8 = v8(42);
        let v6: string = `|${v8}`;
        v8 = lex_0_5;
        v8 = 5;
        let v5: number = v8(v8) + v6;
        const v4: string = `|${v5}`;
        v5 = lex_0_5;
        v6 = undefined;
        return v5(v6) + v4;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static allPositive(param_0, param_1, param_2, param_3): void     {
        const v6 = 0;
        do {
            const v8 = v3;
            const v8 = v3;
            const v9 = v6;
            const v7 = v8[v9];
        } while (v7 >= 0);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static mergeConfigs(param_0, param_1, param_2, param_3, param_4): void     {
        const v7 = {  };
        let v10 = Object;
        let keys = v10.keys;
        keys = v10.keys(v19);
        v10 = 0;
        do {
            let v12 = keys;
            let v12 = keys;
            let v13 = v10;
            let v11 = v12[v13];
            v12 = v7;
            v13 = v11;
            const v14 = v3;
            const v15 = v11;
            v12[v14[v15]] = v13;
            v11 = v10;
            let number: number = Number(v11);
            let v10: number = number + 1;
        } while (number);
        keys = v10.keys;
        keys = v10.keys(v20);
        v10 = 0;
        let v13 = v10;
        let number = v12[v13];
        v12 = v7;
        v13 = number;
        v12[v14[v15]] = v13;
        number = v10;
        number = Number(number);
        let v10: number = number + 1;
        for (let v10 = Object; !(v12.length < number); v12) {
            let v12 = keys;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testCountdown(param_0, param_1, param_2): void     {
        const v4 = "";
        const v3 = 5;
        while (!(v6 < 0)) {
            let v6 = v4;
            const v7 = String;
            const v8 = v3;
            const v4: number = v7(v8) + v6;
            v6 = v3;
        }
    }

    static brightness(param_0, param_1, param_2): void     {
        let v7 = v2;
        let r = v7.r;
        const v5: number = v7.g + r;
        r = v10;
        const v4: number = r.b + v5;
        return 3 / v4;
    }

    static testAllPositive(param_0, param_1, param_2): void     {
        let v6 = lex_0_6;
        let v7: Array<unknown> = [];
        v7 = [];
        v6 = v6(v7);
        v6 = lex_0_6;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        -1[7] = v11;
        3[7] = v12;
        v7 = v7;
        v6 = v6(v7);
        v8 = String;
        v8 = v8(v6);
        v6 = `,${v8}`;
        v8 = String;
        return v6(v6) + v6;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testGuardPattern(param_0, param_1, param_2, param_3): void     { }

    static testMergeConfigs(param_0, param_1, param_2): void     {
        {  }[1] = "a";
        v7 = v3;
        v8 = "b";
        v7[2] = v8;
        v7 = {  };
        v8 = "b";
        v7[20] = v8;
        v7 = v5;
        v8 = "c";
        v7[30] = v8;
        v7 = lex_0_4;
        v8 = v3;
        v7 = v7(v8, v5);
        let v11 = String;
        let v12 = v7;
        v12 = v12[v13];
        v11 = v11(v12);
        v9 = `,${v11}`;
        v11 = String;
        v11 = v7;
        v12 = "b";
        v11 = v11[v12];
        v8 = v11(v11) + v9;
        v7 = `,${v8}`;
        v8 = String;
        v9 = v7;
        v11 = "c";
        v9 = v9[v11];
        return v8(v9) + v7;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static findFirstNegative(param_0, param_1, param_2, param_3): void     {
        const v6 = 0;
        do {
            const v8 = v3;
            const v8 = v3;
            const v9 = v6;
            const v7 = v8[v9];
        } while (v7 > 0);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testFindFirstNegative(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        -2[8] = v14;
        5[8] = v3;
        v9 = 8;
        -v9[8] = v4;
        const v3: Array<unknown> = v8;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */"];
        const v4: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */"];
        v8 = lex_0_1;
        v8 = v8(v3);
        v8 = lex_0_1;
        v8 = v8(v4);
        let v10 = String;
        v10 = v10(v8);
        v8 = `,${v10}`;
        v10 = String;
        v10 = v8;
        return v10(v10) + v8;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}