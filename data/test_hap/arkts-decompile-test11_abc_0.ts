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

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        v2.x = v10;
        v6 = v2;
        v6.y = v11;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
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
        lex_0_1 = func_4;
        throw acc;
    }

    static testForIn(param_0, param_1, param_2): void     {
        {  }[3] = "apple";
        v7 = v4;
        v8 = "banana";
        v7[5] = v8;
        v7 = v4;
        v8 = "cherry";
        v7[7] = v8;
        const v5 = "";
        v8 = Object;
        let keys = v8.keys;
        keys = v8.keys(v4);
        v8 = 0;
        let v10 = keys;
        while (!(v10.length < v9)) {
            let v10 = keys;
            let v11 = v8;
            let v9 = v10[v11];
            let v13 = v5;
            let v12: number = v9 + v13;
            v11 = `:${v12}`;
            v12 = String;
            v13 = v4;
            const v14 = v9;
            v13 = v13[v14];
            v10 = v12(v13) + v11;
            const v5: string = `|${v10}`;
            v9 = v8;
            const number: number = Number(v9);
            let v8: number = number + 1;
            continue;
        }
    }

    static get origin(): void     {
        return new 0();
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testArrayAt(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        v8 = [];
        let v9 = 0;
        const v4 = v8[v9];
        v8 = v3;
        v9 = 1 - v3.length;
        const v6 = v8[v9];
        v8 = v3;
        v9 = 2;
        const v5 = v8[v9];
        length = `,${v4}`;
        v9 = v6 + length;
        v8 = `,${v9}`;
        return v5 + v8;
    }

    static toString(param_0, param_1, param_2): void     {
        let v7 = "(";
        const v8 = String;
        const v9 = v2;
        const x = v9.x;
        let v6: number = v8(x) + v7;
        const v5: string = `,${v6}`;
        v6 = String;
        const y = v7.y;
        const v4: number = v6(y) + v5;
        return `)${v4}`;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static get fromArray(): void     {
        let v7 = v3;
        let v8 = 0;
        const v6 = v7[v8];
        v7 = v13[v9];
        return new v13[v9](v5, v6, v7, v13);
    }

    static get fromPolar(): void     {
        let v8 = v4;
        let v10 = Math;
        let v11 = v3;
        const v5: number = v10.cos(v11) * v8;
        v8 = v16;
        v10 = Math;
        let sin = v10.sin;
        v11 = v15;
        const v6: number = v10.sin(v11) * v8;
        sin = v5;
        v10 = v6;
        return new v10.sin(v11) * v8(v8, sin, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static distanceTo(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const x = v8.x;
        v8 = v14;
        const v4: number = v8.x - x;
        v8 = v13;
        const y = v8.y;
        v8 = v14;
        const v5: number = v8.y - y;
        v8 = Math;
        let v10: number = v4;
        let v9: number = v4 * v10;
        v9 = v5 * v5 + v9;
        return v8.sqrt(v9);
    }

    static testStringChar(param_0, param_1, param_2): void     {
        const v5 = "Hello";
        let v9: string = v5;
        let charAt = v9.charAt;
        charAt = v9.charAt(1);
        v9 = v5;
        let charCodeAt = v9.charCodeAt;
        v10 = 0;
        charCodeAt = v9.charCodeAt(v10);
        v9 = v5;
        let substring = v9.substring;
        v10 = 1;
        substring = v9.substring(v10, 4);
        v11 = charAt;
        v10 = `,${v11}`;
        v11 = String;
        v9 = v11(charCodeAt) + v10;
        substring = `,${v9}`;
        return substring + substring;
    }

    static testMathMethods(param_0, param_1, param_2): void     {
        let v11 = Math;
        let min = v11.min;
        let v12 = 3;
        let v13 = 1;
        let v14 = 4;
        let v15 = 1;
        let v16 = 5;
        min = v11.min(v12, v13, v14, v15);
        v11 = Math;
        let max = v11.max;
        v12 = 3;
        v13 = 1;
        v14 = 4;
        v15 = 1;
        v16 = 5;
        max = v11.max(v12, v13, v14, v15);
        v11 = Math;
        let abs = v11.abs;
        v12 = 42;
        v12 = -v12;
        abs = v11.abs(v12);
        v11 = Math;
        let ceil = v11.ceil;
        v12 = 3.2;
        ceil = v11.ceil(v12);
        v11 = Math;
        let floor = v11.floor;
        v12 = 3.8;
        floor = v11.floor(v12);
        v11 = Math;
        let round = v11.round;
        v12 = 3.5;
        round = v11.round(v12);
        let v20 = String;
        v20 = v20(min);
        let v18: string = `,${v20}`;
        v20 = String;
        v20 = max;
        v16 = `,${v20(v20) + v18}`;
        v17 = String;
        v18 = abs;
        v15 = v17(v18) + v16;
        v14 = `,${v15}`;
        v15 = String;
        v16 = ceil;
        v13 = v15(v16) + v14;
        v12 = `,${v13}`;
        v13 = String;
        v14 = floor;
        v11 = v13(v14) + v12;
        round = `,${v11}`;
        v11 = String;
        v12 = round;
        return v11(v12) + round;
    }

    static testStringMatch(param_0, param_1, param_2): void     {
        const v5 = "Hello World 123";
        const v7 = RegExp;
        let v8 = "\\d+";
        const v3 = new "\\d+"(v7);
        match = v5.match(v3);
        match = match;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testArrayFromMap(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = [];
        const v4: Array<unknown> = [];
        const v7 = 0;
        const v9 = v3;
        while (!(v9.length < v8)) {
            const v9 = v4;
            let push = v9.push;
            let v10 = "n";
            const v11 = String;
            let v12 = v3;
            const v13 = v7;
            v12 = v12[v13];
            v10 = v11(v12) + v10;
            push = v7;
            const number: number = Number(push);
            const v7: number = number + 1;
            continue;
        }
    }

    static testStringSearch(param_0, param_1, param_2): void     {
        const v4 = "hello world";
        const v7: string = v4;
        let search = v7.search;
        let v8 = RegExp;
        v8 = new "world"(v8, "world", v10);
        search = v7.search(v8);
        return search;
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

    static testFactoryMethods(param_0, param_1, param_2): void     {
        let v10 = 3;
        let v11 = 4;
        const v5 = new 4();
        let origin = v10.origin;
        origin = v10.origin();
        origin = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */"];
        const v3: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */"];
        v11 = v3;
        fromArray = v10.fromArray(v11);
        v10 = v5;
        let distanceTo = v10.distanceTo;
        v11 = origin;
        distanceTo = v10.distanceTo(v11);
        const v14 = v5;
        let toString = v14.toString;
        toString = v14.toString();
        v11 = `|${toString}`;
        toString = origin;
        toString = toString.toString;
        v10 = toString.toString() + v11;
        distanceTo = `|${v10}`;
        v10 = String;
        toString = Math;
        let round = toString.round;
        toString = distanceTo;
        round = toString.round(toString);
        return v10(round) + distanceTo;
    }

    static testLogicalDefault(param_0, param_1, param_2, param_3): void     { }

    static testStrictEquality(param_0, param_1, param_2): void     {
        const v7 = 0;
        let v8 = 0;
        const v3: boolean = -v8 === v7;
        v8 = Number;
        let naN = v8.isNaN;
        naN = v8.isNaN(NaN);
        naN = "hello";
        const v5: boolean = naN === "hello";
        let v11 = String;
        v11 = v11(v3);
        v9 = `,${v11}`;
        v11 = String;
        v11 = naN;
        v8 = v11(v11) + v9;
        naN = `,${v8}`;
        v8 = String;
        return v8(v5) + naN;
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

    static testLogicalDefaultChain(param_0, param_1, param_2): void     {
        let v6 = lex_0_1;
        v6 = v6("hello");
        const v4: string = `,${v6}`;
        v6 = lex_0_1;
        v6 = undefined;
        return v6(v6) + v4;
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