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

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v9;
        v2.table = v10;
        v5 = v2;
        v5.conditions = [];
        v5 = v2;
        v5.limitVal = 0;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static on(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = v2;
        let listeners = v8.listeners;
        v8 = v14;
        listeners = listeners[v8];
        if (!(listeners === undefined)) {
            const push = v8.push;
            const v9 = v4;
            return;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static emit(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = v2;
        let listeners = v8.listeners;
        v8 = v12;
        listeners = listeners[v8];
        if (!(listeners === undefined)) {
            return listeners.length;
        }
    }

    static build(param_0, param_1, param_2): void     {
        const v5 = "SELECT * FROM ";
        const v6 = v2;
        const v3: number = v6.table + v5;
        const v7 = v2;
        let conditions = v7.conditions;
        const length = conditions.length;
        if (length < 0) {
            let conditions = v3;
            const length: string = ` WHERE ${conditions}`;
            let v8 = v2;
            conditions = v8.conditions;
            const join = conditions.join;
            v8 = " AND ";
            const v3: number = conditions.join(v8) + length;
        } else {
            const limitVal = join.limitVal;
        }
        return;
    }

    static limit(param_0, param_1, param_2, param_3): void     {
        v2 = v8;
        v2.limitVal = v9;
        return v2;
    }

    static where(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const conditions = v7.conditions;
        return v10;
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
        lex_0_7 = func_0;
        v13 = func_1;
        lex_0_5 = v13;
    }

    static nextState(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(acc === v6)) {
        }
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static testObserver(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v8: acc = acc;
        let v9 = "click";
        let v10 = func_4;
        v8 = acc;
        on = v8.on;
        v9 = "click";
        v10 = func_9;
        v8 = acc;
        let emit = v8.emit;
        v9 = "click";
        v10 = "test";
        emit = v8.emit(v9, v10);
        v8 = acc;
        on = v8.on;
        v9 = "hover";
        v10 = func_18;
        v8 = acc;
        emit = v8.emit;
        v9 = "hover";
        v10 = "x";
        emit = v8.emit(v9, v10);
        v9 = String;
        v10 = emit;
        v9 = v9(v10);
        emit = `,${v9}`;
        v9 = String;
        v9 = emit;
        return v9(v9) + emit;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testArrayFill(param_0, param_1, param_2): void     {
        const v5 = Array;
        let v6 = 5;
        const v3 = new 5(v5);
        v6 = 0;
        do {
            const v8 = v3;
            let v7 = v3;
            const v8 = v6;
            v7[0] = v8;
            v7 = v6;
            let number: number = Number(v7);
            let v6: number = number + 1;
        } while (number);
        do {
            const v8 = v3;
            let number = v3;
            const v8 = v6;
            const v9 = v6;
            number[10 * v9] = v8;
            number = v6;
            number = Number(number);
            let v6: number = number + 1;
        } while (number);
    }

    static testFluentAPI(param_0, param_1, param_2): void     {
        const v3 = new "users"();
        const v13 = v3;
        let where = v13.where;
        where = v13.where("age > 18");
        where = where.where;
        where = "active = 1";
        where = where.where(where);
        let limit = where.limit;
        where = 10;
        limit = where.limit(where);
        let build = limit.build;
        build = limit.build();
        return build;
    }

    static testNestedData(param_0, param_1, param_2): void     {
        const v4 = {  };
        {  }[3] = "pi";
        v11 = v5;
        v12 = "e";
        v11[2] = v12;
        v11 = v4;
        v12 = "math";
        v11[v5] = v12;
        v11 = {  };
        v12 = "c";
        v11[299792] = v12;
        v11 = v7;
        v12 = "g";
        v11[9] = v12;
        v11 = v4;
        v12 = "physics";
        v11[v7] = v12;
        v11 = v4;
        v12 = "math";
        v11 = v11[v12];
        v12 = "pi";
        const v9 = v11[v12];
        v11 = v4;
        v12 = "physics";
        v11 = v11[v12];
        v12 = "c";
        const v3 = v11[v12];
        let v13 = String;
        v13 = v13(v9);
        v11 = `,${v13}`;
        v13 = String;
        return v3(v3) + v11;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testExtendedMath(param_0, param_1, param_2): void     {
        let v11 = Math;
        let pow = v11.pow;
        pow = v11.pow(2, 10);
        v11 = Math;
        let hypot = v11.hypot;
        v12 = 3;
        v13 = 4;
        hypot = v11.hypot(v12, v13);
        v11 = Math;
        let sign = v11.sign;
        v12 = 42;
        v12 = -v12;
        sign = v11.sign(v12);
        v11 = Math;
        let trunc = v11.trunc;
        v12 = 3.7;
        trunc = v11.trunc(v12);
        v11 = Math;
        let log2 = v11.log2;
        v12 = 0x100;
        log2 = v11.log2(v12);
        v11 = Math;
        let cbrt = v11.cbrt;
        v12 = 27;
        cbrt = v11.cbrt(v12);
        let v20 = String;
        v20 = v20(pow);
        let v18: string = `,${v20}`;
        v20 = String;
        v21 = Math;
        let round = v21.round;
        round = v21.round(hypot);
        let v17: number = v20(round) + v18;
        let v16: string = `,${v17}`;
        v17 = String;
        v18 = sign;
        let v15: number = v17(v18) + v16;
        const v14: string = `,${v15}`;
        v15 = String;
        v16 = trunc;
        v13 = v15(v16) + v14;
        v12 = `,${v13}`;
        v13 = String;
        v15 = Math;
        round = v15.round;
        v16 = log2;
        round = v15.round(v16);
        v11 = v13(round) + v12;
        cbrt = `,${v11}`;
        v11 = String;
        v13 = Math;
        round = v13.round;
        round = cbrt;
        round = v13.round(round);
        return v11(round) + cbrt;
    }

    static testNumberChecks(param_0, param_1, param_2): void     {
        let v9 = Number;
        let finite = v9.isFinite;
        finite = v9.isFinite(42);
        v9 = Number;
        finite = v9.isFinite;
        v10 = 1;
        v10 = 0 / v10;
        finite = v9.isFinite(v10);
        v9 = Number;
        let integer = v9.isInteger;
        v10 = 7;
        integer = v9.isInteger(v10);
        v9 = Number;
        integer = v9.isInteger;
        v10 = 3.14;
        integer = v9.isInteger(v10);
        let v14 = String;
        v14 = v14(finite);
        let v12: string = `,${v14}`;
        v14 = String;
        v14 = finite;
        v10 = `,${v14(v14) + v12}`;
        v11 = String;
        v12 = integer;
        v9 = v11(v12) + v10;
        integer = `,${v9}`;
        v9 = String;
        v10 = integer;
        return v9(v10) + integer;
    }

    static testStateMachine(param_0, param_1, param_2): void     {
        let v8 = lex_0_5;
        v8 = v8(v9, "start");
        v8 = lex_0_5;
        let v9 = v8;
        v10 = "pause";
        v8 = v8(v9, v10);
        v8 = lex_0_5;
        v9 = v8;
        v10 = "resume";
        v8 = v8(v9, v10);
        v8 = lex_0_5;
        v9 = v8;
        v10 = "stop";
        v8 = v8(v9, v10);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `->${v14}`;
        v14 = String;
        v10 = `->${v8(v8) + v12}`;
        v11 = String;
        v9 = v11(v8) + v10;
        v8 = `->${v9}`;
        v9 = String;
        v10 = v8;
        return v9(v10) + v8;
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

    static testStringCompare(param_0, param_1, param_2): void     {
        const v3 = "apple";
        const v4 = "banana";
        const v7: string = v3;
        if (v4 < v7) {
            const v5 = "a<b";
        }
        return;
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static divideWithRemainder(param_0, param_1, param_2, param_3, param_4): void     {
        const v5 = 0;
        const v6 = v3;
        do {
            let v9 = v6;
            const v6: number = v4 - v9;
            v9 = v5;
            const v5: number = 1 + v9;
        } while (1 + v9);
    }

    static testDivideRemainder(param_0, param_1, param_2): void     {
        let v5 = lex_0_7;
        v5 = v5(17, 5);
        v7 = String;
        let v8 = v5;
        v8 = v8[v9];
        v7 = v7(v8);
        v5 = `r${v7}`;
        v7 = String;
        v7 = v5;
        v8 = "remainder";
        v7 = v7[v8];
        return v7(v7) + v5;
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