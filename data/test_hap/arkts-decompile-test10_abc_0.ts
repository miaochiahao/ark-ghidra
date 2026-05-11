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

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.parts = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static addOne(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return 1 + v5;
    }

    static double(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return 2 * v5;
    }

    static square(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v9 * v5;
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
        lex_0_3 = func_0;
        v14 = func_1;
        lex_0_4 = v14;
        v14 = func_2;
        lex_0_2 = v14;
        v14 = func_6;
        lex_0_5 = v14;
    }

    static append(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const parts = v7.parts;
        return v10;
    }

    static getFuel(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.fuel;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static describe(param_0, param_1, param_2): void     {
        const v4 = "speed=";
        const v5 = String;
        const v6 = v2;
        const speed = v6.speed;
        return v5(speed) + v4;
    }

    static getSpeed(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.speed;
    }

    static describe(param_0, param_1, param_2): void     {
        const v7 = v2;
        let describe = super.describe;
        describe = v7.describe();
        const v4: string = `,fuel=${describe}`;
        describe = String;
        describe = v10;
        const fuel = describe.fuel;
        return describe(fuel) + v4;
    }

    static describe(param_0, param_1, param_2): void     {
        const v7 = v2;
        let describe = super.describe;
        describe = v7.describe();
        const v4: string = `,doors=${describe}`;
        describe = String;
        describe = v10;
        const doors = describe.doors;
        return describe(doors) + v4;
    }

    static getDoors(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.doors;
    }

    static toString(param_0, param_1, param_2): void     {
        let v6 = v2;
        const parts = v6.parts;
        v6 = "";
        return parts.join(v6);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static fahrenheit(param_0, param_1, param_2): void     {
        const v7 = v2;
        const _celsius = v7._celsius;
        const v5: number = 9 * _celsius;
        const v4: number = 5 / v5;
        return 32 + v4;
    }

    static getCelsius(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4._celsius;
    }

    static testGradeReport(param_0, param_1, param_2): void     {
        let v8 = lex_0_5;
        v8 = v8(95);
        let v6: string = `,${v8}`;
        v8 = lex_0_5;
        v8 = 85;
        let v5: number = v8(v8) + v6;
        const v4: string = `,${v5}`;
        v5 = lex_0_5;
        v6 = 65;
        return v5(v6) + v4;
    }

    static testNestedCalls(param_0, param_1, param_2): void     {
        let v5 = lex_0_2;
        let v6 = lex_0_3;
        let v7 = lex_0_4;
        v7 = v7(3);
        v6 = v6(v7);
        v5 = v5(v6);
        return v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        v2 = v11;
        v2._celsius = 9 / 5 * (32 - v12);
        return;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testBooleanLogic(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v21;
        if (undefined) {
        }
    }

    static testBuilderChain(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        const v15: acc = acc;
        let append = v15.append;
        append = v15.append("Hello");
        append = append.append;
        append = ", ";
        append = append.append(append);
        append = append.append;
        append = "World";
        append = append.append(append);
        append = append.append;
        append = "!";
        append = append.append(append);
        let toString = append.toString;
        toString = append.toString();
        return toString;
    }

    static testGetterSetter(param_0, param_1, param_2): void     {
        let v8 = 0;
        const v5 = new 0();
        let v7 = v5;
        const fahrenheit = v7.fahrenheit;
        v7 = v5;
        v7.fahrenheit = 212;
        let celsius = v8.getCelsius;
        celsius = v5.getCelsius();
        let v9 = String;
        v9 = v9(fahrenheit);
        celsius = `|${v9}`;
        v9 = String;
        v10 = Math;
        let round = v10.round;
        round = v10.round(celsius);
        return v9(round) + celsius;
    }

    static testNestedRecord(param_0, param_1, param_2): void     {
        const v6 = {  };
        {  }[40] = "lat";
        v8 = v3;
        v9 = "lng";
        v8[-v10] = v9;
        v8 = v6;
        v9 = "home";
        v8[v3] = v9;
        v8 = v6;
        v9 = "home";
        v8 = v8[v9];
        v9 = "lat";
        const v5 = v8[v9];
        v8 = String;
        return v8(v5);
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

    static testArraySpliceSlice(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v9: Array<unknown> = v3;
        let slice = v9.slice;
        slice = v9.slice(1, 4);
        v9 = slice;
        let includes = v9.includes;
        v10 = 3;
        includes = v9.includes(v10);
        v9 = slice;
        let reverse = v9.reverse;
        reverse = v9.reverse();
        let v12 = String;
        v12 = v12(slice.length);
        v10 = `,${v12}`;
        v12 = String;
        v12 = includes;
        v9 = v12(v12) + v10;
        reverse = `,${v9}`;
        v9 = String;
        v10 = reverse;
        v12 = 0;
        v10 = v10[v12];
        return v9(v10) + reverse;
    }

    static testConditionalAssign(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        if (v6 <= 90) {
            const v4 = "A";
        }
        return;
    }

    static testMultiLevelInherit(param_0, param_1, param_2): void     {
        let v6 = 100;
        v6 = new 4();
        return v6.describe();
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